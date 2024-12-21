# Define constants for assembly paths
$NpgsqlPath = "./npgsql/lib/net8.0/Npgsql.dll"
$LoggingAbstractionsPath = "./microsoft.extentions.logging.abstractions/lib/net8.0/Microsoft.Extensions.Logging.Abstractions.dll"


try {
    Add-Type -Path $NpgsqlPath
    Add-Type -Path $LoggingAbstractionsPath
    Write-Verbose "Assemblies loaded successfully."
} catch {
    Throw "Failed to load assemblies: $_"
}


# Function to create and open a PostgreSQL connection
function Get-PostgreSQLConnection {
    try {
        $connString = "Host=$Env:db_host;Port=$Env:db_port;Username=$Env:db_user;Password=$Env:db_password;Database=$Env:db_name"
        $conn = New-Object Npgsql.NpgsqlConnection($connString)
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
function Get-PendingCommands {
    param (
        [Parameter(Mandatory)]
        [Npgsql.NpgsqlConnection]$Connection,
        [string]$Id
    )

    $query = "SELECT id, command, arguments FROM commands WHERE command_status = 'PENDING';"
    if ($null -ne $Id){
        $query = "SELECT id, command, arguments FROM commands WHERE id = $Id;"
    }

    try {
        $cmd = $Connection.CreateCommand()
        $cmd.CommandText = $query
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
        [int]$CommandId,

        [Parameter(Mandatory)]
        [ValidateSet("PENDING", "PROCESSING", "COMPLETED")]
        [string]$Status,

        [string]$Result = $null,
        [int] $ExitCode = $null
    )

    try {
        $query = "UPDATE commands SET command_status = @Status"

        if ($Result -ne $null) {
            $query += ", result = @Result"
        }
        if ($ExitCode -ne $null) {
            $query += ", exit_code = @ExitCode"
        }

        $query += ", timestamp = @TimeStamp WHERE id = @CommandId;"
        $cmd = $Connection.CreateCommand()
        $cmd.CommandText = $query
        $cmd.Parameters.Add((New-Object Npgsql.NpgsqlParameter("@Status", $Status))) | Out-Null
        $cmd.Parameters.Add((New-Object Npgsql.NpgsqlParameter("@CommandId", $CommandId))) | Out-Null

        if ($Result -ne $null) {
            $cmd.Parameters.Add((New-Object Npgsql.NpgsqlParameter("@Result", $Result))) | Out-Null
        }
        if ($ExitCode -ne $null) {
            $cmd.Parameters.Add((New-Object Npgsql.NpgsqlParameter("@ExitCode", $ExitCode))) | Out-Null
        }
        $cmd.Parameters.Add((New-Object Npgsql.NpgsqlParameter("@TimeStamp", (Get-Date)))) | Out-Null


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
        $securePassword = ConvertTo-SecureString $Env:ADPassword -AsPlainText -Force
        $credential = New-Object System.Management.Automation.PSCredential ($Env:ADUsername, $securePassword)

        $scriptBlock = {
            param($cmd, $arguments)
            $result = ""
            $exitCode = 0
            try {
                $Command = Get-Command $cmd
                if ($Command.Parameters.ContainsKey("Confirm")) {
                    # Add -Confirm:$false to the arguments
                    $arguments["Confirm"] = $false
                }
                $output = & $cmd @arguments
                $result = $output
                
            } catch {
                $result = $_.Exception.Message.Trim()
                $exitCode = 1
            }
            Write-Output @($result, $exitCode)
        }

        foreach ($passwordArg in ('AccountPassword', 'NewPassword', 'OldPassword')){
            if ($Arguments.ContainsKey($passwordArg)) {
                $Arguments[$passwordArg] = ConvertTo-SecureString $Arguments[$passwordArg] -AsPlainText -Force
            }
        }

        $result = ""
        $exitCode = 0

        $job = Invoke-Command -ComputerName $Env:ADServer -Credential $credential -ScriptBlock $scriptBlock -ArgumentList $ADCommand, $Arguments -AsJob
        try {
            $job | Wait-Job
            $invokeResult = Receive-Job -Job $job
            $result = $invokeResult[0]
            $exitCode = $invokeResult[1]
        }
        catch {
            if ($_.Exception.Message -like "*one or more jobs are blocked waiting for user interaction*") {
                Stop-Job -Job $job
                $result = "You forgot to supply some of the mandatory parameters."
                $exitCode = 1
            }
        }

        # Convert the result to a string representation
        $resultString = if ($result) {
            if ($exitCode -eq 0){
                $result | ConvertTo-Json -AsArray
            } else {
                $result
            }
            
        } else {
            @{
                Message  = "The command $ADCommand completed successfully with no output." 
            } | ConvertTo-Json
        }

        return @{Result = $resultString; ExitCode = $exitCode}
    } catch {
        Write-Error "Failed to execute AD command: $_"
        return $_.Exception.Message
    }
}

function Get-Key {
    param (
        [Parameter(Mandatory)]
        [Npgsql.NpgsqlConnection]$Connection,
        [string]$Id
    )
    try {
        # Generate key pair
        $bobECDH = [System.Security.Cryptography.ECDiffieHellman]::Create([System.Security.Cryptography.ECCurve]::CreateFromFriendlyName("nistP256"))
        
        $query = "SELECT alice_public_key FROM one_time_keys WHERE id = $Id;"
        $cmd = $Connection.CreateCommand()
        $cmd.CommandText = $query
        $reader = $cmd.ExecuteReader()

        $reader.Read()
        $alicePublicKeyBytes = [Convert]::FromBase64String($reader["alice_public_key"])
        $reader.Close()

        # Create an ECDiffieHellman object and import the public key parameters
        $aliceECDH = [System.Security.Cryptography.ECDiffieHellman]::Create()
        $aliceECDH.ImportSubjectPublicKeyInfo($alicePublicKeyBytes, [ref]0) 

        # Derive the shared secret using Bob's ECDH object
        $sharedSecret = $bobECDH.DeriveKeyMaterial($aliceECDH.PublicKey)

        # Convert to Base64 for display or further use
        $sharedSecretBase64 = [Convert]::ToBase64String($sharedSecret)

        Write-Host "Shared Secret (Base64): $sharedSecretBase64"

        $bobPublicKeyBase64 = [Convert]::ToBase64String($bobECDH.ExportSubjectPublicKeyInfo())

        $query += "UPDATE one_time_keys SET bob_public_key = @BobPublicKey WHERE id = $Id;"
        $cmd = $Connection.CreateCommand()
        $cmd.CommandText = $query
        $cmd.Parameters.Add((New-Object Npgsql.NpgsqlParameter("@BobPublicKey", $bobPublicKeyBase64))) | Out-Null
        #$cmd.Parameters.Add((New-Object Npgsql.NpgsqlParameter("@Id", $Id))) | Out-Null
        $cmd.ExecuteNonQuery() | Out-Null

    } catch {
        Write-Error "An error occurred: $_"
    }


}

$notificationHandler = {
    param($origin, $payload)
    $id = $($payload.Payload)
    $channel = $($payload.Channel)
    Write-Host "Received notification from channel $channel with payload $id"
    $conn = Get-PostgreSQLConnection

    if ($channel -eq 'one_time_keys_alice'){
        Get-Key -Connection $conn -Id $id

    } elseif ($channel -eq 'new_commands') {
        $commands = Get-PendingCommands -Connection $conn -Id $id

        if ($commands.Count -gt 0) {
            foreach ($cmd in $commands) {
                # Update status to PROCESSING
                Update-CommandStatus -Connection $conn -CommandId $cmd.Id -Status 'PROCESSING'
    
                Write-Host "Executing command ID $($cmd.Id): $($cmd.Command)"
                $executionResult = Invoke-ADCommand -ADCommand $cmd.Command -Arguments $($cmd.Arguments)
    
                # Update status to DONE with result
                Update-CommandStatus -Connection $conn -CommandId $cmd.Id -Status 'COMPLETED' -Result $executionResult.Result -ExitCode $executionResult.ExitCode
    
                Write-Host "Command ID $($cmd.Id) executed and updated with result."
            }
        }
    }


}

try {
    $conn = Get-PostgreSQLConnection

    Write-Host "Welcome! The script will proceed with listening to the database for new pending commands to execute"
    Write-Host "In order to quit please press 'Ctrl+C'..."
    Write-Host ""
    Write-Host "Listening to the database..."

    $listenCommand = $conn.CreateCommand()
    $listenCommand.CommandText = "LISTEN new_commands; LISTEN one_time_keys_alice;"
    $listenCommand.ExecuteNonQuery() >> $null

    $conn.add_Notification($notificationHandler)

    while ($true) {
        # Wait for a notification
        $conn.Wait(1000) >> $null
    }

} catch {
    Write-Error "An error occurred: $_"
}