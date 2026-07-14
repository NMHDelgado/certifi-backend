package com.enspd.certifi.ml;

import com.enspd.certifi.domain.entity.Document;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Dérive le vecteur de 7 caractéristiques à partir du résultat brut de la
 * vérification cryptographique (cf. mémoire, section 2.2 : "Extraction de
 * caractéristiques").
 */
@Service
public class FeatureExtractionService {

    public FeatureVector extract(
        byte[] submittedFileBytes,
        Document originalDocument,
        boolean hashMatch,
        boolean keyMatch,
        boolean signatureValid,
        boolean replaySuspected
    ) {
        double fileSizeKb = submittedFileBytes.length / 1024.0;

        double sizeDeltaKb = originalDocument != null
            ? fileSizeKb - originalDocument.getFileSizeKb().doubleValue()
            : 0.0;

        double hoursSinceSigning = originalDocument != null
            ? Duration.between(originalDocument.getSignedAt(), OffsetDateTime.now()).toMinutes() / 60.0
            : 0.0;

        return new FeatureVector(
            round(fileSizeKb),
            round(sizeDeltaKb),
            hashMatch,
            keyMatch,
            signatureValid,
            replaySuspected,
            round(hoursSinceSigning)
        );
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }
}
