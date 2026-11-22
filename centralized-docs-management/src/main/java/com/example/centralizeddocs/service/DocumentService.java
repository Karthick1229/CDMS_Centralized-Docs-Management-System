package com.example.centralizeddocs.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.centralizeddocs.dto.DocumentResponse;
import com.example.centralizeddocs.entity.Document;
import com.example.centralizeddocs.repository.DocumentRepository;

@Service
public class DocumentService {
    private static final Set<String> VALID_REPOSITORIES =
            Set.of("ASC_ST", "BANK_ST", "CLAIMS_ST", "LIFE_ST", "IMCO_ST", "PNC_ST", "SDR_ST");

    private final DocumentRepository documentRepository;
    private final DocumentStorageService storageService;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentStorageService storageService) {
        this.documentRepository = documentRepository;
        this.storageService = storageService;
    }

    @Transactional
    public DocumentResponse ingest(
            MultipartFile file, String customerId, String accountNumber,
            String repositoryCode, String documentType, String description,
            String documentCategory, String subCategory, String formStatus,
            Integer retentionYears) {

        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID is required.");
        }
        if (documentType == null || documentType.isBlank()) {
            throw new IllegalArgumentException("Document type is required.");
        }

        String normalizedRepository = repositoryCode == null
                ? "" : repositoryCode.trim().toUpperCase(Locale.ROOT);
        if (!VALID_REPOSITORIES.contains(normalizedRepository)) {
            throw new IllegalArgumentException("Invalid repository code. Use one of: " + VALID_REPOSITORIES);
        }

        int years = retentionYears == null ? 7 : retentionYears;
        if (years < 1 || years > 50) {
            throw new IllegalArgumentException("Retention years must be between 1 and 50.");
        }

        validatePdf(file);
        String storedFileName;
        try {
            storedFileName = storageService.store(file);
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not store the uploaded file.", ex);
        }

        try {
            Document document = new Document();
            document.setCustomerId(customerId.trim());
            document.setAccountNumber(blankToNull(accountNumber));
            document.setRepositoryCode(normalizedRepository);
            document.setDocumentType(documentType.trim());
            document.setDescription(blankToNull(description));
            document.setDocumentCategory(
                    documentCategory == null || documentCategory.isBlank()
                            ? "BANK" : documentCategory.trim());
            document.setSubCategory(blankToNull(subCategory));
            document.setFormStatus(
                    formStatus == null || formStatus.isBlank()
                            ? "ACTIVE" : formStatus.trim().toUpperCase(Locale.ROOT));
            document.setOriginalFileName(file.getOriginalFilename() == null
                    ? "document" : file.getOriginalFilename());
            document.setStoredFileName(storedFileName);
            document.setContentType(file.getContentType() == null
                    ? "application/octet-stream" : file.getContentType());
            document.setRetentionUntil(LocalDate.now().plusYears(years));

            return DocumentResponse.from(documentRepository.save(document));
        } catch (RuntimeException ex) {
            storageService.deleteQuietly(storedFileName);
            throw ex;
        }
    }

    @Transactional
    public List<DocumentResponse> bulkUpload(
            List<MultipartFile> files, String customerId, String accountNumber,
            String repositoryCode, String documentType, String documentCategory,
            Integer retentionYears) {

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Select at least one file for bulk upload.");
        }
        if (files.size() > 100) {
            throw new IllegalArgumentException("A maximum of 100 files is allowed per bulk request.");
        }

        return files.stream()
                .map(file -> ingest(file, customerId, accountNumber, repositoryCode,
                        documentType, "Bulk uploaded document", documentCategory,
                        null, "ACTIVE", retentionYears))
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse getMetadata(Long id) {
        return DocumentResponse.from(findActiveDocument(id));
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> search(
            String customerId, String accountNumber, String documentType,
            String repositoryCode, String documentCategory) {
        return documentRepository.search(
                blankToNull(customerId), blankToNull(accountNumber),
                blankToNull(documentType), blankToNull(repositoryCode),
                blankToNull(documentCategory))
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DownloadDetails getDownloadDetails(Long id) {
        Document document = findActiveDocument(id);
        try {
            Path path = storageService.load(document.getStoredFileName());
            return new DownloadDetails(path, document.getOriginalFileName(), document.getContentType());
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not read the stored document.", ex);
        }
    }

    @Transactional
    public DocumentResponse archive(Long id) {
        Document document = findActiveDocument(id);
        document.setArchived(true);
        return DocumentResponse.from(documentRepository.save(document));
    }

    private Document findActiveDocument(Long id) {
        return documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found: " + id));
    }
    
    private void validatePdf(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null ||
                !fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed.");
        }

        try (InputStream inputStream = file.getInputStream()) {

            byte[] signature = inputStream.readNBytes(5);

            String fileSignature =
                    new String(signature, StandardCharsets.US_ASCII);

            if (!"%PDF-".equals(fileSignature)) {
                throw new IllegalArgumentException(
                        "Invalid PDF file. File content is not a valid PDF.");
            }

        } catch (IOException ex) {
            throw new UncheckedIOException(
                    "Could not validate the uploaded file.", ex);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record DownloadDetails(Path path, String originalFileName, String contentType) {}
}
