package com.enspd.certifi.service;

import com.enspd.certifi.crypto.CryptoService;
import com.enspd.certifi.crypto.KeyManagementService;
import com.enspd.certifi.domain.entity.Document;
import com.enspd.certifi.domain.entity.VerificationRecord;
import com.enspd.certifi.domain.enums.ActionType;
import com.enspd.certifi.domain.enums.CryptoVerdict;
import com.enspd.certifi.domain.repository.DocumentRepository;
import com.enspd.certifi.domain.repository.VerificationRecordRepository;
import com.enspd.certifi.dto.response.SignResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentSigningService {

    private final CryptoService cryptoService;
    private final KeyManagementService keyManagementService;
    private final DocumentRepository documentRepository;
    private final VerificationRecordRepository verificationRecordRepository;
    private final FileValidationService fileValidationService;

    @Transactional
    public SignResponse sign(MultipartFile file, UUID currentUserId) throws Exception {
        fileValidationService.validate(file);

        byte[] fileBytes = file.getBytes();
        byte[] hash = cryptoService.computeSha256(fileBytes);
        byte[] signatureBytes = cryptoService.sign(fileBytes);
        String hashHex = cryptoService.toHex(hash);
        String fingerprint = cryptoService.publicKeyFingerprint(keyManagementService.getPublicKey());

        String signatureFileName = file.getOriginalFilename() + ".sig";

        Document document = Document.builder()
            .fileName(file.getOriginalFilename())
            .sha256Hash(hashHex)
            .publicKeyFingerprint(fingerprint)
            .signatureFileName(signatureFileName)
            .signatureBytes(signatureBytes)
            .fileSizeKb(BigDecimal.valueOf(fileBytes.length / 1024.0))
            .signedAt(OffsetDateTime.now())
            .signedBy(currentUserId)
            .build();

        document = documentRepository.save(document);

        verificationRecordRepository.save(
            VerificationRecord.builder()
                .documentId(document.getId())
                .fileName(document.getFileName())
                .action(ActionType.SIGN)
                .cryptoVerdict(CryptoVerdict.VALID)
                .hashMatch(true)
                .keyMatch(true)
                .signatureValid(true)
                .replaySuspected(false)
                .fileSizeKb(document.getFileSizeKb())
                .sizeDeltaKb(BigDecimal.ZERO)
                .timeSinceSigningHours(BigDecimal.ZERO)
                .performedBy(currentUserId)
                .createdAt(document.getSignedAt())
                .build()
        );

        return new SignResponse(
            document.getId(),
            hashHex,
            fingerprint,
            signatureFileName,
            document.getSignedAt()
        );
    }
}
