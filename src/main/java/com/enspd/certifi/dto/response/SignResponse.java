package com.enspd.certifi.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SignResponse(
    UUID documentId,
    String sha256Hash,
    String publicKeyFingerprint,
    String signatureFileName,
    OffsetDateTime timestamp
) {
}
