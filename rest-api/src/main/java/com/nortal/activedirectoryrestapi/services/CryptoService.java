package com.nortal.activedirectoryrestapi.services;

import com.nortal.activedirectoryrestapi.entities.OneTimeKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

@RequiredArgsConstructor
@Service
public class CryptoService {

    private final NotificationListener notificationListener;
    private final OneTimeKeysService oneTimeKeysService;
    private byte[] sharedSecret;

    public void exchangeKeys() throws Exception {
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
    }

    public String[] encrypt(String plaintext) throws Exception {
            SecretKeySpec sharedKey = new SecretKeySpec(sharedSecret,"AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, sharedKey);
            byte[] iv = cipher.getIV();
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            SecureRandom random = new SecureRandom();
            random.nextBytes(sharedSecret);
            sharedSecret = null;

            return new String[]{Base64.getEncoder().encodeToString(iv), Base64.getEncoder().encodeToString(ciphertext)};
    }
}
