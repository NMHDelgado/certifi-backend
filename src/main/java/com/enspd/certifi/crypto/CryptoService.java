package com.enspd.certifi.crypto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PSSParameterSpec;
import java.security.spec.MGF1ParameterSpec;

/**
 * Couche cryptographique — seule habilitée à établir la validité mathématique
 * d'une signature (cf. prompt maître, section 2 : séparation stricte des
 * couches crypto / ML).
 *
 * Primitives : SHA-256 pour l'empreinte, RSA-2048 avec padding PSS pour la
 * signature (RSASSA-PSS), conformément au besoin fonctionnel n°1 du mémoire.
 */
@Service
@RequiredArgsConstructor
public class CryptoService {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String SIGNATURE_ALGORITHM = "RSASSA-PSS";

    private final KeyManagementService keyManagementService;

    public byte[] computeSha256(byte[] documentBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            return digest.digest(documentBytes);
        } catch (Exception e) {
            throw new CryptoOperationException("Échec du calcul de l'empreinte SHA-256.", e);
        }
    }

    public String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /** Signe le document avec la clé privée RSA du système (padding PSS, empreinte SHA-256). */
    public byte[] sign(byte[] documentBytes) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.setParameter(pssParameterSpec());
            signature.initSign(keyManagementService.getPrivateKey());
            signature.update(documentBytes);
            return signature.sign();
        } catch (Exception e) {
            throw new CryptoOperationException("Échec de la signature du document.", e);
        }
    }

    /** Vérifie la signature avec la clé publique de confiance du système. */
    public boolean verify(byte[] documentBytes, byte[] signatureBytes) {
        return verify(documentBytes, signatureBytes, keyManagementService.getPublicKey());
    }

    /** Variante permettant de vérifier avec une clé publique arbitraire (détection de
     *  substitution de clé : on peut vérifier avec la clé fournie par le document soumis
     *  et comparer son empreinte à celle de la clé de confiance attendue). */
    public boolean verify(byte[] documentBytes, byte[] signatureBytes, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.setParameter(pssParameterSpec());
            signature.initVerify(publicKey);
            signature.update(documentBytes);
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            // Une signature malformée ou incompatible doit être traitée comme invalide,
            // pas comme une erreur serveur.
            return false;
        }
    }

    public String publicKeyFingerprint(PublicKey publicKey) {
        return toHex(computeSha256(publicKey.getEncoded())).substring(0, 32);
    }

    private PSSParameterSpec pssParameterSpec() {
        // Paramètres PSS standards associés à SHA-256 (salt de 32 octets).
        return new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1);
    }
}
