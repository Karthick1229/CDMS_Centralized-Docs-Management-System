package com.example.centralizeddocs.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.centralizeddocs.dto.DocumentResponse;
import com.example.centralizeddocs.service.DocumentService;
import com.example.centralizeddocs.service.RetentionService;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService documentService;
    private final RetentionService retentionService;

    public DocumentController(DocumentService documentService,
                               RetentionService retentionService) {
        this.documentService = documentService;
        this.retentionService = retentionService;
    }
    
    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
            "application", "Centralized Document Management System",
            "status", "Running Successfully",
            "health", "/api/documents/health",
            "repositories", "/api/documents/repositories"
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "application", "Centralized Docs Management");
    }

    @GetMapping("/repositories")
    public List<String> repositories() {
        return List.of("ASC_ST", "BANK_ST", "CLAIMS_ST", "LIFE_ST",
                "IMCO_ST", "PNC_ST", "SDR_ST");
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam String customerId,
            @RequestParam(required = false) String accountNumber,
            @RequestParam String repositoryCode,
            @RequestParam String documentType,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "BANK") String documentCategory,
            @RequestParam(required = false) String subCategory,
            @RequestParam(defaultValue = "ACTIVE") String formStatus,
            @RequestParam(defaultValue = "7") Integer retentionYears,
            @RequestParam("file") MultipartFile file) {

        DocumentResponse response = documentService.ingest(
                file, customerId, accountNumber, repositoryCode, documentType,
                description, documentCategory, subCategory, formStatus, retentionYears);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<DocumentResponse>> bulkUpload(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam String customerId,
            @RequestParam(required = false) String accountNumber,
            @RequestParam String repositoryCode,
            @RequestParam String documentType,
            @RequestParam(defaultValue = "BANK") String documentCategory,
            @RequestParam(defaultValue = "7") Integer retentionYears) {

        return ResponseEntity.ok(documentService.bulkUpload(
                files, customerId, accountNumber, repositoryCode,
                documentType, documentCategory, retentionYears));
    }

    @GetMapping("/search")
    public List<DocumentResponse> search(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String repositoryCode,
            @RequestParam(required = false) String documentCategory) {

        return documentService.search(customerId, accountNumber, documentType,
                repositoryCode, documentCategory);
    }

    @GetMapping("/{id}")
    public DocumentResponse getMetadata(@PathVariable Long id) {
        return documentService.getMetadata(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        DocumentService.DownloadDetails details = documentService.getDownloadDetails(id);
        Resource resource = new FileSystemResource(details.path());

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(details.contentType());
        } catch (Exception ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        String contentDisposition = ContentDisposition.attachment()
                .filename(details.originalFileName(), StandardCharsets.UTF_8)
                .build()
                .toString();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    @PostMapping("/{id}/archive")
    public DocumentResponse archive(@PathVariable Long id) {
        return documentService.archive(id);
    }

    @PostMapping("/retention/run")
    public Map<String, Object> runRetentionNow() {
        int archivedCount = retentionService.archiveExpiredDocuments();
        return Map.of("archivedDocuments", archivedCount, "message", "Retention check completed.");
    }
}
