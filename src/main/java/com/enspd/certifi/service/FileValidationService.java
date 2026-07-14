package com.enspd.certifi.service;

import com.enspd.certifi.exception.InvalidFileException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * Revalidation systématique côté serveur du type et de la taille du fichier
 * (la validation frontend, cf. fileValidator.ts, est un confort UX — jamais
 * une garantie de sécurité à elle seule).
 */
@Service
public class FileValidationService {

    private static final Set<String> ACCEPTED_MIME_TYPES = Set.of(
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Déposez un document pour continuer.");
        }
        if (!ACCEPTED_MIME_TYPES.contains(file.getContentType())) {
            throw new InvalidFileException("Seuls les fichiers PDF et Word (.docx) sont acceptés.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new InvalidFileException("Le fichier dépasse la taille maximale autorisée (10 Mo).");
        }
    }
}
