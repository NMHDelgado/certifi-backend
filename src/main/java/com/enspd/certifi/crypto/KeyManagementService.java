package com.enspd.certifi.crypto;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Gère la paire de clés RSA-2048 du système de signature.
 *
 * Choix d'implémentation assumé pour ce prototype : la paire de clés est
 * générée une seule fois puis persistée au format standard PKCS8 (clé privée)
 * / X.509 SubjectPublicKeyInfo (clé publique) sur le disque du serveur,
 * plutôt que dans un KeyStore avec certificat — ce qui évite une dépendance
 * supplémentaire (Bouncy Castle) pour un besoin non requis par le prototype
 * (pas de chaîne de confiance X.509 à vérifier, une seule clé de confiance).
 *
 * Limite assumée (cf. mémoire, section 4.6.2) : une seule paire de clés de
 * confiance. Une évolution vers une infrastructure multi-signataires (PKI /
 * certificats X.509) est identifiée comme perspective (section 4.6.3).
 *
 * ATTENTION PRODUCTION : la clé privée ne doit jamais être stockée en clair
 * sur disque dans un déploiement réel. Utilisez un HSM ou un coffre-fort de
 * secrets (Vault, AWS KMS, etc.) pour protéger `certifi-private.key`.
 */
@Service
public class KeyManagementService {

    @Value("${certifi.crypto.keystore-path}")
    private String keyDirectoryPath;

    @Getter
    private PublicKey publicKey;

    @Getter
    private PrivateKey privateKey;

    @PostConstruct
    public void init() throws Exception {
        File keyDir = new File(keyDirectoryPath).getParentFile() == null
            ? new File(".")
            : new File(keyDirectoryPath).getParentFile();
        keyDir.mkdirs();

        File privateKeyFile = new File(keyDir, "certifi-private.key");
        File publicKeyFile = new File(keyDir, "certifi-public.key");

        if (privateKeyFile.exists() && publicKeyFile.exists()) {
            loadExistingKeyPair(privateKeyFile, publicKeyFile);
        } else {
            generateAndPersistKeyPair(privateKeyFile, publicKeyFile);
        }
    }

    private void loadExistingKeyPair(File privateKeyFile, File publicKeyFile) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        byte[] privateBytes = Files.readAllBytes(privateKeyFile.toPath());
        privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));

        byte[] publicBytes = Files.readAllBytes(publicKeyFile.toPath());
        publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicBytes));
    }

    private void generateAndPersistKeyPair(File privateKeyFile, File publicKeyFile) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

        Files.write(privateKeyFile.toPath(), privateKey.getEncoded());
        Files.write(publicKeyFile.toPath(), publicKey.getEncoded());
    }
}
