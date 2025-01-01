class SessionManager{

    [System.Management.Automation.Runspaces.PSSession] $psSession
    [string]$hostname
    [string]$username
    [string]$keyFilePath
    [hashtable]$sshOptions

    SessionManager($hostname, $username, $keyFilePath, $sshOptions){
        $this.psSession = $null
        $this.hostname = $hostname
        $this.username = $username
        $this.keyFilePath = $keyFilePath
        $this.sshOptions = $sshOptions
    }

    [void] StartPSSession([boolean]$verbose=$false) {
        if ($verbose){Write-Host "Starting new PSSession..." -ForegroundColor Green}
        
        $this.psSession = New-PSSession -HostName $this.hostname -UserName $this.username -KeyFilePath $this.keyFilePath -Options $this.sshOptions
        if ($null -eq $this.psSession){
            Throw "Error starting PSSession"
        }

        if ($verbose){Write-Host "PSSession started successfully." -ForegroundColor Green}
    }

    [void] RestartPSSessionIfNeeded([boolean]$verbose=$false){
        if ($null -eq $this.psSession){
            throw "PSSession is not started yet!"
        }

        if ($this.psSession.State -ne "Opened") {
            Write-Host "PSSession is in a $($this.psSession.State) state. Attempting to restart..." -ForegroundColor Yellow
            Remove-PSSession -Session $this.psSession -ErrorAction SilentlyContinue
            $this.StartPSSession($verbose)
        }
    }

}