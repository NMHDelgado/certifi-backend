package com.enspd.certifi.config;

import com.enspd.certifi.domain.entity.AppUser;
import com.enspd.certifi.domain.enums.UserRole;
import com.enspd.certifi.domain.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provisionne automatiquement un compte administrateur au demarrage de
 * l'application.
 *
 * Volontairement PAS un @RestController : AuthService interdit toute
 * creation de compte ADMIN via une route HTTP publique (cf. commentaire
 * de RegisterRequest) afin d'eviter une elevation de privilege par un
 * appelant non authentifie. Le provisionnement se fait donc uniquement
 * cote serveur, au lancement, via CommandLineRunner.
 *
 * Idempotent : si l'email admin existe deja en base, ne fait rien (pas de
 * doublon, pas de reinitialisation du mot de passe a chaque redemarrage).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SeederController implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${certifi.admin.email}")
    private String adminEmail;

    @Value("${certifi.admin.password}")
    private String adminPassword;

    @Value("${certifi.admin.full-name}")
    private String adminFullName;

    @Override
    @Transactional
    public void run(String... args) {
        String normalizedEmail = adminEmail.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            log.info("Seeder admin : le compte '{}' existe deja, aucune action.", normalizedEmail);
            return;
        }

        AppUser admin = AppUser.builder()
            .email(normalizedEmail)
            .passwordHash(passwordEncoder.encode(adminPassword))
            .role(UserRole.ADMIN)
            .build();

        userRepository.save(admin);

        log.warn(
            "Seeder admin : compte administrateur cree ({}). " +
            "Changez le mot de passe par defaut avant tout deploiement en production !",
            normalizedEmail
        );
    }
}
