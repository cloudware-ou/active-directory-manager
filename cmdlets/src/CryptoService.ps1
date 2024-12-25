
class CryptoService {
    [byte[]]$sharedSecret

    CryptoService() {
        $this.sharedSecret = $null
    }

    [byte[]]ExchangeKeys ([byte[]] $alicePublicKeyDer){
        try {
            $asnReader = [System.Formats.Asn1.AsnReader]::new($alicePublicKeyDer, [System.Formats.Asn1.AsnEncodingRules]::DER)
            $outerSequence = $asnReader.ReadSequence()
            $outerSequence.ReadSequence()
            $unusedBits = 0
            $alicePublicKey = $outerSequence.ReadBitString([ref]$unusedBits)
    
            $bobKeyPair = [Sodium.PublicKeyBox]::GenerateKeyPair()
            $bobPrivateKey = $bobKeyPair.PrivateKey
            $bobPublicKey = $bobKeyPair.PublicKey
    
            $this.sharedSecret = [Sodium.ScalarMult]::Mult($bobPrivateKey, $alicePublicKey)
    
            $asnWriter = [System.Formats.Asn1.AsnWriter]::new([System.Formats.Asn1.AsnEncodingRules]::DER)
            $asnWriter.PushSequence()
            $asnWriter.PushSequence()
            $asnWriter.WriteObjectIdentifier("1.3.101.110")  # OID for Curve25519
            $asnWriter.PopSequence()
            $asnWriter.WriteBitString($bobPublicKey)
            $asnWriter.PopSequence()
    
            $bobPublicKeyDer = $asnWriter.Encode()
    
            return $bobPublicKeyDer
        } catch {
            Throw "An error occurred: $_"
        }
    
    
    }

    [string]Decrypt([string]$CiphertextBase64, [string]$IVBase64){

        try{
            $ciphertext = [Convert]::FromBase64String($CiphertextBase64)
            $iv = [Convert]::FromBase64String($IVBase64)

            $aes = [System.Security.Cryptography.Aes]::Create()
            $aes.Key = $this.sharedSecret
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

    [void] EraseSharedSecret() {
        # Erase shared secret
        for ($i = 0; $i -lt $this.sharedSecret.Length; $i++) {
            $this.sharedSecret[$i] = Get-Random -Minimum 0 -Maximum 256
        }
        
        # Set the variable to null
        $this.sharedSecret = $null
    }

    [bool] HasValidSharedSecret(){
        return $null -ne $this.sharedSecret
    }
}