package com.enspd.certifi.ml;

/** Vecteur de 7 caractéristiques, identique au contrat consommé par le ml-service (section 3 du prompt maître). */
public record FeatureVector(
    double fileSizeKb,
    double sizeDeltaKb,
    boolean hashMatch,
    boolean keyMatch,
    boolean signatureValid,
    boolean replaySuspected,
    double timeSinceSigningHours
) {
}
