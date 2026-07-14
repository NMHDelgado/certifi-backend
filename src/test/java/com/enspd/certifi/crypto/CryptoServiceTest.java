package com.enspd.certifi.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * NOTE : ce test n'a pas pu être exécuté dans l'environnement de génération
 * (pas d'accès à Maven Central pour résoudre les dépendances). À exécuter
 * avec `mvn test` dans un environnement disposant d'un accès réseau standard.
 */
class CryptoServiceTest {

    private CryptoService cryptoService;
    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();

        KeyManagementService keyManagementService = mock(KeyManagementService.class);
        when(keyManagementService.getPrivateKey()).thenReturn(keyPair.getPrivate());
        when(keyManagementService.getPublicKey()).thenReturn(keyPair.getPublic());

        cryptoService = new CryptoService(keyManagementService);
    }

    @Test
    void signAndVerify_roundTrip_succeeds() {
        byte[] document = "contenu du document à signer".getBytes(StandardCharsets.UTF_8);

        byte[] signature = cryptoService.sign(document);
        boolean valid = cryptoService.verify(document, signature);

        assertTrue(valid);
    }

    @Test
    void verify_withTamperedDocument_fails() {
        byte[] original = "contenu original".getBytes(StandardCharsets.UTF_8);
        byte[] tampered = "contenu modifié".getBytes(StandardCharsets.UTF_8);

        byte[] signature = cryptoService.sign(original);
        boolean valid = cryptoService.verify(tampered, signature);

        assertFalse(valid);
    }

    @Test
    void computeSha256_isDeterministic() {
        byte[] content = "abc".getBytes(StandardCharsets.UTF_8);

        String hash1 = cryptoService.toHex(cryptoService.computeSha256(content));
        String hash2 = cryptoService.toHex(cryptoService.computeSha256(content));

        assertEquals(hash1, hash2);
        assertEquals(64, hash1.length()); // 32 octets en hexadécimal
    }

    @Test
    void publicKeyFingerprint_isStableAndShort() {
        String fingerprint1 = cryptoService.publicKeyFingerprint(keyPair.getPublic());
        String fingerprint2 = cryptoService.publicKeyFingerprint(keyPair.getPublic());

        assertEquals(fingerprint1, fingerprint2);
        assertEquals(32, fingerprint1.length());
    }
}
