package com.enspd.certifi.dto.response;

import com.enspd.certifi.domain.enums.UserRole;

import java.util.UUID;

public record LoginResponse(
    UUID id,
    String email,
    UserRole role,
    String token
) {
}
