package com.example.centralizeddocs.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.centralizeddocs.entity.Document;
import com.example.centralizeddocs.repository.DocumentRepository;

@Service
public class RetentionService {
    private final DocumentRepository documentRepository;

    public RetentionService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    // Runs every day at 2:00 AM using the server's local time.
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public int archiveExpiredDocuments() {
        List<Document> expiredDocuments =
                documentRepository.findByArchivedFalseAndDeletedFalseAndRetentionUntilLessThanEqual(
                        LocalDate.now());

        expiredDocuments.forEach(document -> document.setArchived(true));
        documentRepository.saveAll(expiredDocuments);
        return expiredDocuments.size();
    }
}
