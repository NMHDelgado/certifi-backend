package com.enspd.certifi.service;

import com.enspd.certifi.domain.entity.Alert;
import com.enspd.certifi.domain.entity.Document;
import com.enspd.certifi.domain.entity.VerificationRecord;
import com.enspd.certifi.domain.enums.AlertType;
import com.enspd.certifi.domain.enums.PredictedClass;
import com.enspd.certifi.domain.repository.AlertRepository;
import com.enspd.certifi.dto.response.AlertDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Besoin fonctionnel n°6 du mémoire : notifier un administrateur lorsqu'une
 * signature invalide ou une anomalie est détectée.
 */
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final EmailAlertService emailAlertService;

    @Transactional
    public void raiseIfNeeded(VerificationRecord record, Document originalDocument) {
        if (record.isMlServiceUnavailable()) {
            createAndNotify(
                AlertType.ML_SERVICE_DOWN,
                "Le service de classification ML est indisponible. Le verdict cryptographique "
                    + "reste fiable, mais aucune classification d'anomalie n'a pu être produite pour \""
                    + record.getFileName() + "\".",
                record.getDocumentId()
            );
            return;
        }

        PredictedClass predicted = record.getPredictedClass();
        if (predicted == null || predicted == PredictedClass.NORMAL) {
            return;
        }

        AlertType type = switch (predicted) {
            case FALSIFICATION -> AlertType.FALSIFICATION;
            case SUBSTITUTION_CLE -> AlertType.SUBSTITUTION_CLE;
            case REJEU -> AlertType.REJEU;
            case NORMAL -> null;
        };

        if (type != null) {
            createAndNotify(
                type,
                "Anomalie détectée sur le document \"" + record.getFileName() + "\" : " + predicted + ".",
                record.getDocumentId()
            );
        }
    }

    private void createAndNotify(AlertType type, String message, UUID documentId) {
        Alert alert = Alert.builder()
            .documentId(documentId)
            .type(type)
            .message(message)
            .acknowledged(false)
            .build();
        alertRepository.save(alert);
        emailAlertService.notifyAdmin(alert);
    }

    @Transactional(readOnly = true)
    public List<AlertDto> listAll() {
        return alertRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(a -> new AlertDto(a.getId(), a.getDocumentId(), a.getType(), a.getMessage(), a.isAcknowledged(), a.getCreatedAt()))
            .toList();
    }

    @Transactional
    public void acknowledge(UUID alertId) {
        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new com.enspd.certifi.exception.DocumentNotFoundException("Alerte introuvable."));
        alert.setAcknowledged(true);
        alertRepository.save(alert);
    }
}
