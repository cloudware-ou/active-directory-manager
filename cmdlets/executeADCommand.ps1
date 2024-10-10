
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


$command = "Get-ADUser -Filter * -Property Name | Select-Object -First 50 Name"

#$command = 'New-ADUser -Name "Alicee Johnson" -GivenName "Alicee" -Surname "Johnsonn" -SamAccountName "ajohnson011" -UserPrincipalName "ajohnnson01@domain.com" -Path "CN=Users,DC=Domain,DC=ee" -AccountPassword (ConvertTo-SecureString "ComplexP@ssw0rd4567" -AsPlainText -Force) -Enabled $true'
$result = Execute-ADCommand -ADCommand $command
$result
