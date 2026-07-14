package com.enspd.certifi.domain.repository;

import com.enspd.certifi.domain.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Optional<Document> findBySha256Hash(String sha256Hash);
}
