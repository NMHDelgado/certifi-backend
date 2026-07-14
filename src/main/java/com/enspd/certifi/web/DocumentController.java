package com.enspd.certifi.web;

import com.enspd.certifi.domain.entity.AppUser;
import com.enspd.certifi.dto.response.HistoryPageDto;
import com.enspd.certifi.dto.response.SignResponse;
import com.enspd.certifi.dto.response.VerifyResponse;
import com.enspd.certifi.domain.enums.ActionType;
import com.enspd.certifi.domain.enums.PredictedClass;
import com.enspd.certifi.service.DocumentSigningService;
import com.enspd.certifi.service.DocumentVerificationService;
import com.enspd.certifi.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentSigningService documentSigningService;
    private final DocumentVerificationService documentVerificationService;
    private final HistoryService historyService;

    @PostMapping(value = "/sign", consumes = "multipart/form-data")
    public SignResponse sign(
        @RequestParam("file") MultipartFile file,
        @AuthenticationPrincipal AppUser currentUser
    ) throws Exception {
        return documentSigningService.sign(file, currentUser != null ? currentUser.getId() : null);
    }

    @PostMapping(value = "/verify", consumes = "multipart/form-data")
    public VerifyResponse verify(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "documentId", required = false) UUID documentId,
        @AuthenticationPrincipal AppUser currentUser
    ) throws Exception {
        return documentVerificationService.verify(file, documentId, currentUser != null ? currentUser.getId() : null);
    }

    @GetMapping("/history")
    public HistoryPageDto history(
        @RequestParam(required = false) ActionType action,
        @RequestParam(required = false) PredictedClass predictedClass,
        @RequestParam(required = false) String fileName,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        return historyService.search(action, predictedClass, fileName, page, pageSize);
    }
}
