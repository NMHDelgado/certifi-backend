package com.enspd.certifi.service;

import com.enspd.certifi.domain.entity.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAlertService {

    private final JavaMailSender mailSender;

    @Value("${certifi.alerts.admin-email}")
    private String adminEmail;

    @Value("${certifi.alerts.from-address}")
    private String fromAddress;

    public void notifyAdmin(Alert alert) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(adminEmail);
            message.setSubject("[Certifi] Alerte : " + alert.getType());
            message.setText(alert.getMessage());
            mailSender.send(message);
        } catch (Exception e) {
            // Une panne SMTP ne doit jamais faire échouer la vérification en cours :
            // l'alerte reste de toute façon persistée et visible dans /alertes.
            log.error("Échec de l'envoi de l'e-mail d'alerte à {} : {}", adminEmail, e.getMessage());
        }
    }
}
