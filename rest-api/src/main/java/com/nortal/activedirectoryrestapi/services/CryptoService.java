package com.nortal.activedirectoryrestapi.services;

import com.nortal.activedirectoryrestapi.entities.OneTimeKeys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.KeyAgreement;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;
import java.util.Base64;

@RequiredArgsConstructor
@Service
public class CryptoService {

    private final NotificationListener notificationListener;
    private final OneTimeKeysService oneTimeKeysService;

    @PostConstruct
    public void generateKeys() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair aliceKeyPair = kpg.generateKeyPair();


            ECPublicKey alicePublicKey = (ECPublicKey) aliceKeyPair.getPublic();

            String AliceX = Base64.getEncoder().encodeToString(alicePublicKey.getW().getAffineX().toByteArray());
            String AliceY = Base64.getEncoder().encodeToString(alicePublicKey.getW().getAffineY().toByteArray());

            Long id = oneTimeKeysService.saveOneTimeKeys(AliceX, AliceY);
            System.out.println("Alice key: " + AliceX + " and alice key: " + AliceY);
            OneTimeKeys oneTimeKeys = notificationListener.getOneTimeKeys(id);
            System.out.println("here");

            String bobX = oneTimeKeys.getBobX();
            String bobY = oneTimeKeys.getBobY();

            AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
            params.init(new ECGenParameterSpec("secp256r1"));
            ECParameterSpec ecSpec = params.getParameterSpec(ECParameterSpec.class);

            // Create the ECPoint and ECPublicKeySpec
            byte[] xBytes = Base64.getDecoder().decode(bobX);
            byte[] yBytes = Base64.getDecoder().decode(bobY);

            BigInteger x = new BigInteger(1, xBytes);
            BigInteger y = new BigInteger(1, yBytes);
            ECPoint point = new ECPoint(x, y);
            ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, ecSpec);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey bobPublicKey = keyFactory.generatePublic(pubKeySpec);

            // Compute shared secret
            KeyAgreement ka = KeyAgreement.getInstance("ECDH");
            ka.init(aliceKeyPair.getPrivate());
            ka.doPhase(bobPublicKey, true);

            byte[] sharedSecret = ka.generateSecret();
            System.out.println("Shared secret: " + Base64.getEncoder().encodeToString(sharedSecret));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
