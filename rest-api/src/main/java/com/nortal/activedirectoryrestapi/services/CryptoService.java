package com.nortal.activedirectoryrestapi.services;

import com.nortal.activedirectoryrestapi.entities.OneTimeKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

@RequiredArgsConstructor
@Service
public class CryptoService {

    private final NotificationListener notificationListener;
    private final OneTimeKeysService oneTimeKeysService;

    public void generateKeys() {
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

            byte[] sharedSecret = ka.generateSecret();
            System.out.println("Shared secret: " + Base64.getEncoder().encodeToString(sharedSecret));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
