package com.enspd.certifi.dto.response;

import com.enspd.certifi.domain.enums.ActionType;
import com.enspd.certifi.domain.enums.CryptoVerdict;
import com.enspd.certifi.domain.enums.PredictedClass;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HistoryItemDto(
    UUID id,
    String fileName,
    ActionType action,
    CryptoVerdict cryptoVerdict,
    PredictedClass predictedClass,
    OffsetDateTime date
) {
}
