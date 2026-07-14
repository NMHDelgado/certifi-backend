package com.enspd.certifi.domain.entity;

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
@Table(name = "document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "sha256_hash", nullable = false)
    private String sha256Hash;

    @Column(name = "public_key_fingerprint", nullable = false)
    private String publicKeyFingerprint;

    @Column(name = "signature_file_name", nullable = false)
    private String signatureFileName;

    @Lob
    @Column(name = "signature_bytes", nullable = false)
    private byte[] signatureBytes;

    @Column(name = "file_size_kb", nullable = false)
    private BigDecimal fileSizeKb;

    @Column(name = "signed_at", nullable = false)
    @Builder.Default
    private OffsetDateTime signedAt = OffsetDateTime.now();

    @Column(name = "signed_by")
    private UUID signedBy;
}
