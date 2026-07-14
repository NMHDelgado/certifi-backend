package com.enspd.certifi.domain.entity;

import com.enspd.certifi.domain.enums.ActionType;
import com.enspd.certifi.domain.enums.CryptoVerdict;
import com.enspd.certifi.domain.enums.PredictedClass;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "document_id")
    private UUID documentId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType action;

    @Enumerated(EnumType.STRING)
    @Column(name = "crypto_verdict")
    private CryptoVerdict cryptoVerdict;

    @Column(name = "hash_match")
    private Boolean hashMatch;

    @Column(name = "key_match")
    private Boolean keyMatch;

    @Column(name = "signature_valid")
    private Boolean signatureValid;

    @Column(name = "replay_suspected")
    private Boolean replaySuspected;

    @Enumerated(EnumType.STRING)
    @Column(name = "predicted_class")
    private PredictedClass predictedClass;

    @Column(name = "probability_normal")
    private BigDecimal probabilityNormal;

    @Column(name = "probability_falsification")
    private BigDecimal probabilityFalsification;

    @Column(name = "probability_substitution")
    private BigDecimal probabilitySubstitution;

    @Column(name = "probability_rejeu")
    private BigDecimal probabilityRejeu;

    @Column(name = "file_size_kb")
    private BigDecimal fileSizeKb;

    @Column(name = "size_delta_kb")
    private BigDecimal sizeDeltaKb;

    @Column(name = "time_since_signing_hours")
    private BigDecimal timeSinceSigningHours;

    @Column(name = "ml_service_unavailable", nullable = false)
    @Builder.Default
    private boolean mlServiceUnavailable = false;

    @Column(name = "ml_model_version")
    private String mlModelVersion;

    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
