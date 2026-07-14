package com.enspd.certifi.dto.response;

public record FeatureVectorDto(
    double fileSizeKb,
    double sizeDeltaKb,
    boolean hashMatch,
    boolean keyMatch,
    boolean signatureValid,
    boolean replaySuspected,
    double timeSinceSigningHours
) {
}
