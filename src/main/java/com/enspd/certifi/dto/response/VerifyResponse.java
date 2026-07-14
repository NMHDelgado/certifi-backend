package com.enspd.certifi.dto.response;

import com.enspd.certifi.domain.enums.CryptoVerdict;
import com.enspd.certifi.domain.enums.PredictedClass;

import java.time.OffsetDateTime;

public record VerifyResponse(
    CryptoVerdict cryptoVerdict,
    boolean hashMatch,
    boolean keyMatch,
    boolean signatureValid,
    boolean replaySuspected,
    PredictedClass predictedClass,           // null si mlServiceUnavailable = true
    ClassProbabilitiesDto classProbabilities, // null si mlServiceUnavailable = true
    FeatureVectorDto featureVector,
    boolean mlServiceUnavailable,
    OffsetDateTime timestamp
) {
}
