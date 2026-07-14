package com.enspd.certifi.service;

import com.enspd.certifi.crypto.CryptoService;
import com.enspd.certifi.crypto.KeyManagementService;
import com.enspd.certifi.domain.entity.Document;
import com.enspd.certifi.domain.entity.VerificationRecord;
import com.enspd.certifi.domain.enums.ActionType;
import com.enspd.certifi.domain.enums.CryptoVerdict;
import com.enspd.certifi.domain.enums.PredictedClass;
import com.enspd.certifi.domain.repository.DocumentRepository;
import com.enspd.certifi.domain.repository.VerificationRecordRepository;
import com.enspd.certifi.dto.response.ClassProbabilitiesDto;
import com.enspd.certifi.dto.response.FeatureVectorDto;
import com.enspd.certifi.dto.response.VerifyResponse;
import com.enspd.certifi.ml.ClassificationResult;
import com.enspd.certifi.ml.FeatureExtractionService;
import com.enspd.certifi.ml.FeatureVector;
import com.enspd.certifi.ml.MlClassifierClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Orchestre la vérification d'un document : couche cryptographique d'abord
 * (seule habilitée à statuer sur la validité mathématique de la signature),
 * puis couche ML qui qualifie la nature de l'anomalie éventuelle. Les deux
 * décisions restent strictement distinctes dans la réponse renvoyée.
 */
@Service
@RequiredArgsConstructor
public class DocumentVerificationService {

    private final CryptoService cryptoService;
    private final KeyManagementService keyManagementService;
    private final DocumentRepository documentRepository;
    private final VerificationRecordRepository verificationRecordRepository;
    private final FeatureExtractionService featureExtractionService;
    private final MlClassifierClient mlClassifierClient;
    private final FileValidationService fileValidationService;
    private final AlertService alertService;

    @Transactional
    public VerifyResponse verify(MultipartFile file, UUID documentIdHint, UUID currentUserId) throws Exception {
        fileValidationService.validate(file);

        byte[] submittedBytes = file.getBytes();
        byte[] computedHash = cryptoService.computeSha256(submittedBytes);
        String computedHashHex = cryptoService.toHex(computedHash);

        Document originalDocument = documentIdHint != null
            ? documentRepository.findById(documentIdHint).orElse(null)
            : documentRepository.findBySha256Hash(computedHashHex).orElse(null);

        boolean hashMatch = originalDocument != null && originalDocument.getSha256Hash().equals(computedHashHex);
        boolean keyMatch = true; // le prototype ne gère qu'une seule clé de confiance (cf. limites, mémoire 4.6.2)
        boolean signatureValid = originalDocument != null
            && cryptoService.verify(submittedBytes, originalDocument.getSignatureBytes(), keyManagementService.getPublicKey());

        boolean replaySuspected = originalDocument != null
            && signatureValid
            && verificationRecordRepository.existsByDocumentIdAndCryptoVerdict(originalDocument.getId(), CryptoVerdict.VALID);

        CryptoVerdict cryptoVerdict = (signatureValid && hashMatch && keyMatch) ? CryptoVerdict.VALID : CryptoVerdict.INVALID;

        FeatureVector featureVector = featureExtractionService.extract(
            submittedBytes, originalDocument, hashMatch, keyMatch, signatureValid, replaySuspected
        );

        Optional<ClassificationResult> classification = mlClassifierClient.classify(featureVector);

        VerificationRecord.VerificationRecordBuilder recordBuilder = VerificationRecord.builder()
            .documentId(originalDocument != null ? originalDocument.getId() : null)
            .fileName(file.getOriginalFilename())
            .action(ActionType.VERIFY)
            .cryptoVerdict(cryptoVerdict)
            .hashMatch(hashMatch)
            .keyMatch(keyMatch)
            .signatureValid(signatureValid)
            .replaySuspected(replaySuspected)
            .fileSizeKb(BigDecimal.valueOf(featureVector.fileSizeKb()))
            .sizeDeltaKb(BigDecimal.valueOf(featureVector.sizeDeltaKb()))
            .timeSinceSigningHours(BigDecimal.valueOf(featureVector.timeSinceSigningHours()))
            .performedBy(currentUserId)
            .createdAt(OffsetDateTime.now());

        ClassProbabilitiesDto probabilitiesDto = null;
        PredictedClass predictedClass = null;
        boolean mlServiceUnavailable = classification.isEmpty();

        if (classification.isPresent()) {
            ClassificationResult result = classification.get();
            predictedClass = result.predictedClass();
            probabilitiesDto = new ClassProbabilitiesDto(
                result.probabilities().getOrDefault(PredictedClass.NORMAL, 0.0),
                result.probabilities().getOrDefault(PredictedClass.FALSIFICATION, 0.0),
                result.probabilities().getOrDefault(PredictedClass.SUBSTITUTION_CLE, 0.0),
                result.probabilities().getOrDefault(PredictedClass.REJEU, 0.0)
            );
            recordBuilder
                .predictedClass(predictedClass)
                .probabilityNormal(BigDecimal.valueOf(probabilitiesDto.NORMAL()))
                .probabilityFalsification(BigDecimal.valueOf(probabilitiesDto.FALSIFICATION()))
                .probabilitySubstitution(BigDecimal.valueOf(probabilitiesDto.SUBSTITUTION_CLE()))
                .probabilityRejeu(BigDecimal.valueOf(probabilitiesDto.REJEU()))
                .mlModelVersion(result.modelVersion());
        } else {
            recordBuilder.mlServiceUnavailable(true);
        }

        VerificationRecord savedRecord = verificationRecordRepository.save(recordBuilder.build());

        alertService.raiseIfNeeded(savedRecord, originalDocument);

        return new VerifyResponse(
            cryptoVerdict,
            hashMatch,
            keyMatch,
            signatureValid,
            replaySuspected,
            predictedClass,
            probabilitiesDto,
            new FeatureVectorDto(
                featureVector.fileSizeKb(),
                featureVector.sizeDeltaKb(),
                hashMatch,
                keyMatch,
                signatureValid,
                replaySuspected,
                featureVector.timeSinceSigningHours()
            ),
            mlServiceUnavailable,
            savedRecord.getCreatedAt()
        );
    }
}
