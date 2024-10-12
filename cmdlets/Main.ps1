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

            $query = 'SELECT command FROM ad_commands' # Select only the command field
            $data = Invoke-PostgreSQL -Connection $postgresql -Query $query
            
            if ($data) {
                 
               # Execute each command retrieved from the database
                foreach ($row in $data) {
                    $command = $row.command
                    Write-Host "Executing command: $command"
                    $result = Execute-ADCommand -ADCommand $command
                    $result
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
        $result = Invoke-Expression -Command $ADCommand
        return $result
    } catch {
        Write-Error "Failed to execute AD command: $_"
        return $_.Exception.Message
    }
}



# Connect to the database and execute commands
Connect-ToPostgreSQL -User "postgres" -Password "9112" -Database "active_directory_commands" -Server "localhost"
