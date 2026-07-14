package com.enspd.certifi.domain.repository;

import com.enspd.certifi.domain.entity.VerificationRecord;
import com.enspd.certifi.domain.enums.ActionType;
import com.enspd.certifi.domain.enums.PredictedClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface VerificationRecordRepository extends JpaRepository<VerificationRecord, UUID> {

    /** Utilisé pour la détection de rejeu : un document déjà vérifié une première fois
     *  et présenté à nouveau, alors qu'il est par ailleurs cryptographiquement valide. */
    boolean existsByDocumentIdAndCryptoVerdict(UUID documentId, com.enspd.certifi.domain.enums.CryptoVerdict cryptoVerdict);

    @Query("""
    SELECT v FROM VerificationRecord v
    WHERE (:action IS NULL OR v.action = :action)
      AND (:predictedClass IS NULL OR v.predictedClass = :predictedClass)
      AND (:fileName IS NULL OR LOWER(v.fileName) LIKE LOWER(CONCAT('%', CAST(:fileName AS string), '%')))
    ORDER BY v.createdAt DESC
    """)
    Page<VerificationRecord> search(
            @Param("action") ActionType action,
            @Param("predictedClass") PredictedClass predictedClass,
            @Param("fileName") String fileName,
            Pageable pageable
    );
}
