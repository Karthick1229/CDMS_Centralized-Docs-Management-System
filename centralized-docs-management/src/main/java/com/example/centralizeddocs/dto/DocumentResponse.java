package com.example.centralizeddocs.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.centralizeddocs.entity.Document;

public record DocumentResponse(
        String documentId,
        String customerId,
        String accountNumber,
        String repositoryCode,
        String documentType,
        String description,
        String documentCategory,
        String subCategory,
        String formStatus,
        String originalFileName,
        LocalDateTime uploadedAt,
        LocalDate retentionUntil,
        boolean archived) {

    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                String.format("%06d", document.getId()),
                document.getCustomerId(),
                document.getAccountNumber(),
                document.getRepositoryCode(),
                document.getDocumentType(),
                document.getDescription(),
                document.getDocumentCategory(),
                document.getSubCategory(),
                document.getFormStatus(),
                document.getOriginalFileName(),
                document.getUploadedAt(),
                document.getRetentionUntil(),
                document.isArchived());
    }
}
