package com.enspd.certifi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "L'adresse e-mail est requise.")
    @Email(message = "Format d'adresse e-mail invalide.")
    String email,

    @NotBlank(message = "Le mot de passe est requis.")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères.")
    String password
) {
}
