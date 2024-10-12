function Connect-ToPostgreSQL {
    param (
        [string]$User,
        [string]$Password,
        [string]$Database,
        [string]$Server,
        [int]$Port = 5432
    )

    # Check if the PostgreSQLCmdlets module is installed, if not, install it
    if (-not (Get-Module -ListAvailable -Name PostgreSQLCmdlets)) {
        Install-Module PostgreSQLCmdlets -Force -Scope CurrentUser
    }

    try {
        $postgresql = Connect-PostgreSQL -User $User -Password $Password -Database $Database -Server $Server -Port $Port

        if ($postgresql) {
            Write-Host "Connection to the database '$Database' on server '$Server' was successful!"

            $query = 'SELECT id, command FROM ad_commands' # Select only the command field and the ID for updating status
            $data = Invoke-PostgreSQL -Connection $postgresql -Query $query
            
            if ($data) {
                # Execute each command retrieved from the database
                foreach ($row in $data) {
                    $command = $row.command
                    $commandId = $row.id # Assuming there's an ID column to identify the command

                    Write-Host "Executing command: $command"
                    $result = Execute-ADCommand -ADCommand $command

                    # Convert the result to a string
                    $resultString = $result | Out-String

                    # Prepare the SQL command to update command status and result
                    $updateQuery = "UPDATE ad_commands SET command_status = 'done', result = @Result WHERE id = @CommandId"
                    $params = @{
                        '@Result'     = $resultString.Trim() 
                        '@CommandId'  = $commandId
                    }

                    # Execute the update query
                    Invoke-PostgreSQL -Connection $postgresql -Query $updateQuery -Params $params
                    Write-Host "Command executed and updated in the database."
                }
            } else {
                Write-Host "No data found in the 'ad_commands' table."
            }

        } else {
            Write-Host "Connection failed!"
        }
    } catch {
        Write-Host "An error occurred while trying to connect: $_"
    }
}

function Execute-ADCommand {
    param (
        [string]$ADCommand
    )

    try {
        # Execute the AD command and capture the result
        $result = Invoke-Expression -Command $ADCommand
        
        # Check if the result is null or empty
        if ($result) {
            return $result
        } else {
            return "No output from command."
        }
    } catch {
        Write-Error "Failed to execute AD command: $_"
        return $_.Exception.Message
    }
}

# Connect to the database and execute commands
Connect-ToPostgreSQL -User "postgres" -Password "9112" -Database "active_directory_commands" -Server "localhost"
