package com.enspd.certifi.domain.repository;

import com.enspd.certifi.domain.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findAllByOrderByCreatedAtDesc();
    long countByAcknowledgedFalse();
}
