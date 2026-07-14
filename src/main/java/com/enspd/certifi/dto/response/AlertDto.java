package com.enspd.certifi.dto.response;

import com.enspd.certifi.domain.enums.AlertType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertDto(
    UUID id,
    UUID documentId,
    AlertType type,
    String message,
    boolean acknowledged,
    OffsetDateTime createdAt
) {
}
