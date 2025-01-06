package com.nortal.activedirectoryrestapi.services;

import com.nortal.activedirectoryrestapi.entities.OneTimeKeys;
import com.nortal.activedirectoryrestapi.exceptions.TerminatingError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@RequiredArgsConstructor
@Service
public class CryptoService {
    private final NotificationListener notificationListener;
    private final OneTimeKeysService oneTimeKeysService;
    private byte[] sharedSecret;

    /**
     * Performs Diffie-Hellman key exchange
     */
        public void exchangeKeys() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("X25519");
            KeyPair aliceKeyPair = kpg.generateKeyPair();
            String alicePublicKeyBase64 = Base64.getEncoder().encodeToString(aliceKeyPair.getPublic().getEncoded());

            Long id = oneTimeKeysService.saveOneTimeKeys(alicePublicKeyBase64);
            OneTimeKeys oneTimeKeys = notificationListener.getOneTimeKeys(id);

            String bobPublicKeyBase64 = oneTimeKeys.getBobPublicKey();
            byte[] bobPublicKeyBytes = Base64.getDecoder().decode(bobPublicKeyBase64);

            KeyFactory keyFactory = KeyFactory.getInstance("X25519");
            X509EncodedKeySpec bobKeySpec = new X509EncodedKeySpec(bobPublicKeyBytes);
            PublicKey bobPublicKey = keyFactory.generatePublic(bobKeySpec);

            // Compute shared secret
            KeyAgreement ka = KeyAgreement.getInstance("X25519");
            ka.init(aliceKeyPair.getPrivate());
            ka.doPhase(bobPublicKey, true);
            sharedSecret = ka.generateSecret();
        } catch (Exception e) {
            throw new TerminatingError("Error while generating shared secret", e);
        }
    }

    /**
     * Encrypts bytes with AES algorithm using the derived shared key.
     * @param bytes bytes to encrypt.
     * @return a 2-element array with IV and ciphertext, both encoded in base64.
     */
    public String[] encrypt(byte[] bytes) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(sharedSecret,"AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] iv = cipher.getIV();
            byte[] ciphertext = cipher.doFinal(bytes);
            return new String[]{Base64.getEncoder().encodeToString(iv), Base64.getEncoder().encodeToString(ciphertext)};
        } catch (Exception e) {
            throw new TerminatingError("Encryption error", e);
        }
    }

    /**
     * Securely erases the shared secret with random bytes and then sets to null.
     */
    public void eraseSharedSecret() {
        securelyEraseByteArray(sharedSecret);
        sharedSecret = null;
    }

    /**
     * Checks if shared secret is not null
     * @return true if shared secret is not null, false otherwise.
     */
    public boolean hasValidSharedSecret() {
        return sharedSecret != null;
    }

    public void securelyEraseByteArray(byte[] bytes) {
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
    }

    public void securelyEraseCharArray(char[] chars) {
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) random.nextInt(Character.MAX_VALUE); // Random char in the valid range
        }
    }

}
