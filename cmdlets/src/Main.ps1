try {
    Import-Module ../npgsql/lib/net8.0/Npgsql.dll
    Import-Module ../microsoft.extentions.logging.abstractions/lib/net8.0/Microsoft.Extensions.Logging.Abstractions.dll
    Import-Module ../sodium.core/lib/netstandard2.1/Sodium.Core.dll
    Import-Module ./CryptoService.ps1
    Import-Module ./SessionManager.ps1
    Write-Verbose "Assemblies loaded successfully."
} catch {
    Throw "Failed to load assemblies: $_"
}

$Global:mutex = New-Object System.Threading.Mutex $false, "NotificationHandlerMutex"
$Global:cryptoService = [CryptoService]::new()

# Define the SSH options in a hashtable
$sshOptions = @{
    ServerAliveInterval = 30  # Send a keepalive message every 30 seconds
    ServerAliveCountMax = 2   # Terminate the connection if no response after 2 keepalive messages
    TCPKeepAlive = "yes"      # Enable TCP keepalive messages
}

$privatekeyfile = "../privatekey"
$Global:sessionManager = [SessionManager]::new($Env:ADServer, $Env:ADUsername, $privatekeyfile, $sshOptions)

# Function to create and open a PostgreSQL connection
function Get-PostgreSQLConnection {
    try {
        $connString = "Host=$Env:db_host;Port=$Env:db_port;Username=$Env:db_user;Password=$Env:db_password;Database=$Env:db_name;Include Error Detail=true"
        $conn = [Npgsql.NpgsqlConnection]::new($connString)
        $conn.Open()

        if ($conn.State -eq 'Open') {
            Write-Verbose "Connected to database '$Database' on server '$Server'."
            return $conn
        } else {
            Throw "Failed to open connection to the database."
        }
    } catch {
        Throw "Error connecting to PostgreSQL: $_"
    }
}

# Function to retrieve pending commands from the database
function Get-PendingCommand {
    param (
        [Parameter(Mandatory)]
        [Npgsql.NpgsqlConnection]$Connection,
        [Parameter(Mandatory)]
        [long]$Id
    )

    $query = "SELECT id, command, arguments FROM commands WHERE id = @Id;"

    try {
        $cmd = $Connection.CreateCommand()
        $cmd.CommandText = $query
        $cmd.Parameters.AddWithValue("@Id", $Id) | Out-Null
        $reader = $cmd.ExecuteReader()
        $data = @()
        while ($reader.Read()) {
            $data += [PSCustomObject]@{
                Id      = $reader["id"]
                Command = $reader["command"]
                Arguments = $reader["arguments"] | ConvertFrom-Json -AsHashtable
            }
        }
        $reader.Close()
        Write-Verbose "$($data.Count) pending command(s) retrieved."
        return $data
    } catch {
        Throw "Error retrieving commands: $_"
    }
}

# Function to update the status of a command
function Update-CommandStatus {
    param (
        [Parameter(Mandatory)]
        [Npgsql.NpgsqlConnection]$Connection,

        [Parameter(Mandatory)]
        [long]$CommandId,

        [Parameter(Mandatory)]
        [ValidateSet("PENDING", "PROCESSING", "COMPLETED")]
        [string]$Status,

        $Result = $null,
        [int] $ExitCode = $null
    )

    try {
        $query = "UPDATE commands SET command_status = @Status"

        if ($null -ne $Result) {
            $query += ", result = @Result"
        }
        if ($null -ne $ExitCode) {
            $query += ", exit_code = @ExitCode"
        }

        $query += ", timestamp = @TimeStamp WHERE id = @CommandId;"
        $cmd = $Connection.CreateCommand()
        $cmd.CommandText = $query
        $cmd.Parameters.AddWithValue("@Status", $Status) | Out-Null
        $cmd.Parameters.AddWithValue("@CommandId", $CommandId) | Out-Null

        if ($null -ne $Result) {
            $param = [Npgsql.NpgsqlParameter]::new("@Result", [NpgsqlTypes.NpgsqlDbType]::Json)
            $param.Value = $Result
            $cmd.Parameters.Add($param) | Out-Null
        }
        if ($null -ne $ExitCode) {
            $cmd.Parameters.AddWithValue("@ExitCode", $ExitCode) | Out-Null
        }
        $cmd.Parameters.AddWithValue("@TimeStamp", (Get-Date)) | Out-Null

        $cmd.ExecuteNonQuery() | Out-Null
        Write-Verbose "Command ID $CommandId updated to status '$Status'."
    } catch {
        Throw "Error updating command status: $_"
    }
}


# Function to execute an AD command on a remote server
function Invoke-ADCommand {
    param (
        [Parameter(Mandatory)]
        [string]$ADCommand,
        [hashtable]$Arguments
    )

    try {
        $scriptBlock = {
            param($cmd, $arguments)
            try {
                $Command = Get-Command $cmd
                if ($Command.Parameters.ContainsKey("Confirm")) {
                    # Add -Confirm:$false to the arguments
                    $arguments["Confirm"] = $false
                }
                $output = & $cmd @arguments
                Write-Output @($output, 0)
            } catch [Microsoft.ActiveDirectory.Management.ADIdentityAlreadyExistsException]{
                Write-Output @($_.Exception.Message, 409)
            } catch [Microsoft.ActiveDirectory.Management.ADIdentityNotFoundException]{
                Write-Output @($_.Exception.Message, 404)
            } catch {
                Write-Output @($_.Exception.Message, 400)
            }
        }

        foreach ($passwordArg in ('AccountPassword', 'NewPassword', 'OldPassword')){
            if ($Arguments.ContainsKey($passwordArg)) {
                $enc = $Arguments[$passwordArg]
                $byteArray = $Global:cryptoService.Decrypt($enc["ciphertext"], $enc["iv"])

                $password = [SecureString]::new()
                
                foreach ($a in $byteArray){
                    $password.AppendChar([char]$a)
                }
                [Array]::Clear($byteArray, 0, $byteArray.Length)
                $Arguments[$passwordArg] = $password
            }
        }

        if ($Global:cryptoService.HasValidSharedSecret) {
            $Global:cryptoService.EraseSharedSecret()
        }

        $Global:sessionManager.RestartPSSessionIfNeeded($true)

        $result = ""
        $exitCode = 0
        
        try {
            $job = Invoke-Command -Session $Global:sessionManager.psSession -ScriptBlock $scriptBlock -ArgumentList $ADCommand, $Arguments -AsJob
            $job | Wait-Job -Timeout 30
            $invokeResult = Receive-Job -Job $job
            $result = $invokeResult[0]
            $exitCode = $invokeResult[1]
        }
        catch {
            if ($_.Exception.Message -like "*one or more jobs are blocked waiting for user interaction*") {
                $result = "You forgot to supply some of the mandatory parameters."
            } elseif ($null -eq $invokeResult) {
                $result = "A network error occured."
            } else {
                $result = $_.Exception.Message.Trim()
            }
            $exitCode = 1
            Stop-Job -Job $job
        } finally {
            Remove-Job -Job $job
        }

        if ($exitCode -ne 0){
            Write-Host "Command completed with error"  -ForegroundColor Red
        }

        # Convert the result to a string representation
        $resultString = if ($result) {
            if ($exitCode -eq 0){
                $result | ConvertTo-Json -AsArray
                
            } else {
                @{
                    ErrorMessage = $result
                } | ConvertTo-Json
            }

        } else {

            @{
                Message  = "The command $ADCommand completed successfully with no output." 
            } | ConvertTo-Json
        }

        return @{Result = $resultString; ExitCode = $exitCode}
    } catch {
        Throw "Failed to execute AD command: $_"
    }
}

function HandleKeys{
    param(
        [Npgsql.NpgsqlConnection]$Connection,
        [long]$Id
    )

    $query = "SELECT alice_public_key FROM one_time_keys WHERE id = @Id;"
    $cmd = $Connection.CreateCommand()
    $cmd.CommandText = $query
    $cmd.Parameters.AddWithValue("@Id", $Id) | Out-Null
    $reader = $cmd.ExecuteReader()

    $reader.Read()
    $alicePublicKeyDer = [Convert]::FromBase64String($reader["alice_public_key"])
    $reader.Close()

    $bobPublicKeyDer = $Global:cryptoService.ExchangeKeys($alicePublicKeyDer)
    $bobPublicKeyBase64 = [Convert]::ToBase64String($bobPublicKeyDer)

    $query = "UPDATE one_time_keys SET bob_public_key = @BobPublicKey WHERE id = @Id;"
    $cmd = $Connection.CreateCommand()
    $cmd.CommandText = $query
    $cmd.Parameters.AddWithValue("@BobPublicKey", $bobPublicKeyBase64) | Out-Null
    $cmd.Parameters.AddWithValue("@Id", $Id) | Out-Null
    $cmd.ExecuteNonQuery() | Out-Null
}

function HandleCommand{
    param(
        [Parameter(Mandatory)]
        [Npgsql.NpgsqlConnection]$conn,
        [long]$id
    )

    $commands = Get-PendingCommand -Connection $conn -Id $id

    if ($commands.Count -gt 0) {
        foreach ($cmd in $commands) {
            # Update status to PROCESSING
            Update-CommandStatus -Connection $conn -CommandId $cmd.Id -Status 'PROCESSING'

            Write-Host "Executing command ID $($cmd.Id): $($cmd.Command)"
            $executionResult = Invoke-ADCommand -ADCommand $cmd.Command -Arguments $($cmd.Arguments)

            # Update status to COMPLETED with result
            Update-CommandStatus -Connection $conn -CommandId $cmd.Id -Status 'COMPLETED' -Result $executionResult.Result -ExitCode $executionResult.ExitCode

            Write-Host "Command ID $($cmd.Id) executed and updated with result."
        }
    } else {
        Write-Error "No command with ID $id, shouldn't happen."
    }
}

function HandleNotification{
    param (
        [long]$id,
        [string]$channel
    )

    Write-Host "Received notification from channel $channel with payload $id" -ForegroundColor Cyan
    $conn = Get-PostgreSQLConnection

    if ($channel -eq 'one_time_keys_alice'){
        HandleKeys -Connection $conn -Id $id
    } elseif ($channel -eq 'new_commands') {
        HandleCommand -conn $conn -id $id
    } else {
        Write-Error "Wrong channel, shouldn't happen."
    }
    $conn.Close()

}

$notificationHandler = {
    param($origin, $payload)
    try{
        $Global:mutex.WaitOne()
        HandleNotification -id $($payload.Payload) -channel $($payload.Channel)
    } catch {
        Write-Error $_
    } finally {
        $Global:mutex.ReleaseMutex()
    }
}



try {
    $Global:sessionManager.StartPSSession($true)
    $conn = Get-PostgreSQLConnection
    Write-Host "Welcome! The script will proceed with listening to the database for new pending commands to execute"
    Write-Host "In order to quit please press 'Ctrl+C'..."
    Write-Host ""
    Write-Host "Listening to the database..."

    $listenCommand = $conn.CreateCommand()
    $listenCommand.CommandText = "LISTEN new_commands; LISTEN one_time_keys_alice;"
    $listenCommand.ExecuteNonQuery() | Out-Null

    $conn.add_Notification($notificationHandler)

    while ($true) {
        # Wait for a notification
        $conn.Wait(1000) | Out-Null
    }
    $conn.Close()
} catch {
    Throw "An error occurred: $_"
}