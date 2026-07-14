package com.enspd.certifi.service;

import com.enspd.certifi.domain.entity.VerificationRecord;
import com.enspd.certifi.domain.enums.ActionType;
import com.enspd.certifi.domain.enums.PredictedClass;
import com.enspd.certifi.domain.repository.VerificationRecordRepository;
import com.enspd.certifi.dto.response.HistoryItemDto;
import com.enspd.certifi.dto.response.HistoryPageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final VerificationRecordRepository verificationRecordRepository;

    @Transactional(readOnly = true)
    public HistoryPageDto search(ActionType action, PredictedClass predictedClass, String fileName, int page, int pageSize) {
        Page<VerificationRecord> result = verificationRecordRepository.search(
            action, predictedClass, fileName, PageRequest.of(Math.max(page - 1, 0), pageSize)
        );

        var items = result.getContent().stream()
            .map(r -> new HistoryItemDto(r.getId(), r.getFileName(), r.getAction(), r.getCryptoVerdict(), r.getPredictedClass(), r.getCreatedAt()))
            .toList();

        return new HistoryPageDto(items, page, pageSize, result.getTotalElements());
    }
}
