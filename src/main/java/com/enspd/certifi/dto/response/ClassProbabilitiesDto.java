package com.enspd.certifi.dto.response;

public record ClassProbabilitiesDto(
    double NORMAL,
    double FALSIFICATION,
    double SUBSTITUTION_CLE,
    double REJEU
) {
}
