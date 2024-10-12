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

            $query = 'SELECT * FROM ad_commands'
            $data = Invoke-PostgreSQL -Connection $postgresql -Query $query
            
            if ($data) {
                Write-Host "Data retrieved from 'ad_commands' table:"
                $data | Format-Table -AutoSize # Format the output as a table for better readability
            } else {
                Write-Host "No data found in the 'ad_commands' table."
            }

            return $data # Return the retrieved data if needed
        } else {
            Write-Host "Connection failed!"
        }
    } catch {
        Write-Host "An error occurred while trying to connect: $_"
    }
}

Connect-ToPostgreSQL -User "postgres" -Password "9112" -Database "active_directory_commands" -Server "localhost"
