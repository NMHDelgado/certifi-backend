package com.enspd.certifi.ml;

import com.enspd.certifi.domain.enums.PredictedClass;

import java.util.Map;

public record ClassificationResult(
    PredictedClass predictedClass,
    Map<PredictedClass, Double> probabilities,
    String modelVersion
) {
}
