try {
    Add-Type -Path "./npgsql/lib/net8.0/Npgsql.dll"
    Add-Type -Path "./microsoft.extentions.logging.abstractions/lib/net8.0/Microsoft.Extensions.Logging.Abstractions.dll"
    Add-Type -Path "./sodium.core/lib/netstandard2.1/Sodium.Core.dll"
    Write-Verbose "Assemblies loaded successfully."
} catch {
    Throw "Failed to load assemblies: $_"
}

$global:sharedSecret = $null
$global:privatekeyfile = "privatekey"
$Global:MySession = $null
$Global:mutex = New-Object System.Threading.Mutex $false, "NotificationHandlerMutex"

# Define the SSH options in a hashtable
$Global:sshOptions = @{
    ServerAliveInterval = 30  # Send a keepalive message every 30 seconds
    ServerAliveCountMax = 2   # Terminate the connection if no response after 2 keepalive messages
    TCPKeepAlive = "yes"      # Enable TCP keepalive messages
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

function Decrypt{
    param(
        [Parameter(Mandatory)]
        [string]$CiphertextBase64,
        [Parameter(Mandatory)]
        [string]$IVBase64
    )

    try{
        $ciphertext = [Convert]::FromBase64String($CiphertextBase64)
        $iv = [Convert]::FromBase64String($IVBase64)

        $aes = [System.Security.Cryptography.Aes]::Create()
        $aes.Key = $global:sharedSecret
        $aes.IV = $iv
        $aes.Mode = [System.Security.Cryptography.CipherMode]::CBC
        $aes.Padding = [System.Security.Cryptography.PaddingMode]::PKCS7

        $decryptor = $aes.CreateDecryptor()

        # Decrypt the message
        $memoryStream = New-Object System.IO.MemoryStream
        $cryptoStream = New-Object System.Security.Cryptography.CryptoStream($memoryStream, $decryptor, [System.Security.Cryptography.CryptoStreamMode]::Write)
        $cryptoStream.Write($ciphertext, 0, $ciphertext.Length)
        $cryptoStream.Close()
        $DecryptedData = $memoryStream.ToArray()
        $memoryStream.Close()
        


        return [Text.Encoding]::UTF8.GetString($DecryptedData)
    } catch {
        Throw "An error occured: $_"
    }
}

function EraseSharedSecret {
    if ($null -ne $global:sharedSecret){
        # Erase shared secret
        for ($i = 0; $i -lt $global:sharedSecret.Length; $i++) {
            $global:sharedSecret[$i] = Get-Random -Minimum 0 -Maximum 256
        }
        
        # Set the variable to null
        $global:sharedSecret = $null
    }
}

# Function to retrieve pending commands from the database
function Get-PendingCommand {
    param (
        [Parameter(Mandatory)]
        [Npgsql.NpgsqlConnection]$Connection,
        [Parameter(Mandatory)]
        [string]$Id
    )

    $query = "SELECT id, command, arguments FROM commands WHERE id = $Id;"

    try {
        Write-Host "Get-PendingCommand 1"
        $cmd = $Connection.CreateCommand()
        $cmd.CommandText = $query
        $reader = $cmd.ExecuteReader()
        Write-Host "Get-PendingCommand 2"
        $data = @()
        while ($reader.Read()) {
            $data += [PSCustomObject]@{
                Id      = $reader["id"]
                Command = $reader["command"]
                Arguments = $reader["arguments"] | ConvertFrom-Json -AsHashtable
            }
        }
        $reader.Close()
        Write-Host "Get-PendingCommand 3"
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

        if ($null -ne $Result) {
            $query += ", result = @Result"
        }
        if ($null -ne $ExitCode) {
            $query += ", exit_code = @ExitCode"
        }

        $query += ", timestamp = @TimeStamp WHERE id = @CommandId;"
        $cmd = $Connection.CreateCommand()
        $cmd.CommandText = $query
        $cmd.Parameters.Add((New-Object Npgsql.NpgsqlParameter("@Status", $Status))) | Out-Null
        $cmd.Parameters.Add((New-Object Npgsql.NpgsqlParameter("@CommandId", $CommandId))) | Out-Null

        if ($null -ne $Result) {
            $cmd.Parameters.Add((New-Object Npgsql.NpgsqlParameter("@Result", $Result))) | Out-Null
        }
        if ($null -ne $ExitCode) {
            $cmd.Parameters.Add((New-Object Npgsql.NpgsqlParameter("@ExitCode", $ExitCode))) | Out-Null
        }
        $cmd.Parameters.Add((New-Object Npgsql.NpgsqlParameter("@TimeStamp", (Get-Date)))) | Out-Null


        $cmd.ExecuteNonQuery() | Out-Null
        Write-Verbose "Command ID $CommandId updated to status '$Status'."
    } catch {
        Throw "Error updating command status: $_"
    }
}

function Start-PSSession {
    Write-Host "Starting a new PSSession..." -ForegroundColor Green
    $Global:MySession = New-PSSession -HostName $Env:ADServer -UserName $Env:ADUsername -KeyFilePath $global:privatekeyfile -Options $Global:sshOptions
    Write-Host "PSSession started successfully." -ForegroundColor Green
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
                $enc = $Arguments[$passwordArg]
                $Arguments[$passwordArg] = ConvertTo-SecureString (Decrypt -CiphertextBase64 $enc["ciphertext"] -IVBase64 $enc["iv"]) -AsPlainText -Force
            }
        }

        EraseSharedSecret

        if ($null -eq $Global:MySession -or $Global:MySession.State -ne "Opened") {
            Write-Host "PSsession state: $($Global:MySession.State), $Global:MySession"
            Write-Host "PSSession is not active. Attempting to restart..." -ForegroundColor Yellow
            if ($null -ne $Global:MySession) {
                Remove-PSSession -Session $Global:MySession -ErrorAction SilentlyContinue
            }
            Start-PSSession
        }

        $result = ""
        $exitCode = 0
        
        try {
            $job = Invoke-Command -Session $Global:MySession -ScriptBlock $scriptBlock -ArgumentList $ADCommand, $Arguments -AsJob
            $job | Wait-Job -Timeout 30
            $invokeResult = Receive-Job -Job $job
            $result = $invokeResult[0]
            $exitCode = $invokeResult[1]
        }
        catch {
            if ($_.Exception.Message -like "*one or more jobs are blocked waiting for user interaction*") {
                Stop-Job -Job $job
                $result = "You forgot to supply some of the mandatory parameters."
                
            } else {
                $result = $_.Exception.Message
            }
            $exitCode = 1
        }

        if ($exitCode -eq 1){
            Write-Host "Command completed with error"
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
        Throw "Failed to execute AD command: $_"
    }
}

function ExchangeKeys {
    param (
        [Parameter(Mandatory)]
        [Npgsql.NpgsqlConnection]$Connection,
        [string]$Id
    )
    try {
        $query = "SELECT alice_public_key FROM one_time_keys WHERE id = $Id;"
        $cmd = $Connection.CreateCommand()
        $cmd.CommandText = $query
        $reader = $cmd.ExecuteReader()

        $reader.Read()
        $alicePublicKeyDer = [Convert]::FromBase64String($reader["alice_public_key"])
        $reader.Close()

        $asnReader = [System.Formats.Asn1.AsnReader]::new($alicePublicKeyDer, [System.Formats.Asn1.AsnEncodingRules]::DER)
        $outerSequence = $asnReader.ReadSequence()
        $outerSequence.ReadSequence()
        $unusedBits = 0
        $alicePublicKey = $outerSequence.ReadBitString([ref]$unusedBits)

        $bobKeyPair = [Sodium.PublicKeyBox]::GenerateKeyPair()
        $bobPrivateKey = $bobKeyPair.PrivateKey
        $bobPublicKey = $bobKeyPair.PublicKey

        $global:sharedSecret = [Sodium.ScalarMult]::Mult($bobPrivateKey, $alicePublicKey)

        $asnWriter = [System.Formats.Asn1.AsnWriter]::new([System.Formats.Asn1.AsnEncodingRules]::DER)
        $asnWriter.PushSequence()
        $asnWriter.PushSequence()
        $asnWriter.WriteObjectIdentifier("1.3.101.110")  # OID for Curve25519
        $asnWriter.PopSequence()
        $asnWriter.WriteBitString($bobPublicKey)
        $asnWriter.PopSequence()

        $bobPublicKeyDer = $asnWriter.Encode()

        $bobPublicKeyBase64 = [Convert]::ToBase64String($bobPublicKeyDer)

        $query = "UPDATE one_time_keys SET bob_public_key = @BobPublicKey WHERE id = $Id;"
        $cmd = $Connection.CreateCommand()
        $cmd.CommandText = $query
        $cmd.Parameters.Add((New-Object Npgsql.NpgsqlParameter("@BobPublicKey", $bobPublicKeyBase64))) | Out-Null
        #$cmd.Parameters.Add((New-Object Npgsql.NpgsqlParameter("@Id", $Id))) | Out-Null
        $cmd.ExecuteNonQuery() | Out-Null

    } catch {
        Throw "An error occurred: $_"
    }


}

function HandleCommand{
    param(
        [Parameter(Mandatory)]
        [Npgsql.NpgsqlConnection]$conn,
        [string]$id
    )

    $commands = Get-PendingCommand -Connection $conn -Id $id

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
    } else {
        Write-Error "No command with ID $id, shouldn't happen."
    }
}

function HandleNotification{
    param (
        [string]$id,
        [string]$channel
    )

    Write-Host "Received notification from channel $channel with payload $id"
    $conn = Get-PostgreSQLConnection

    if ($channel -eq 'one_time_keys_alice'){
        ExchangeKeys -Connection $conn -Id $id
    } elseif ($channel -eq 'new_commands') {
        HandleCommand -conn $conn -id $id
    } else {
        Write-Error "Wrong channel, shouldn't happen."
    }
    $conn.Close()

}

$notificationHandler = {
    param($origin, $payload)

    $Global:mutex.WaitOne()
    HandleNotification -id $($payload.Payload) -channel $($payload.Channel)
    $Global:mutex.ReleaseMutex()
}

try {
    Start-PSSession
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

} catch {
    throw "An error occurred: $_"
} finally {
    $conn.Close()
}