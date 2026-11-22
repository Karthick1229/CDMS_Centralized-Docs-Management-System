package com.example.centralizeddocs.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".pdf", ".png", ".jpg", ".jpeg", ".txt");

    @Value("${app.storage.location:./document-storage}")
    private String storageLocation;

    private Path rootLocation;

    @PostConstruct
    public void initialize() throws IOException {
        rootLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
        Files.createDirectories(rootLocation);
    }

    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a non-empty document file.");
        }

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());

        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex).toLowerCase(Locale.ROOT);
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Allowed file types: PDF, PNG, JPG, JPEG and TXT.");
        }

        String storedName = UUID.randomUUID() + extension;
        Path destination = rootLocation.resolve(storedName).normalize();
        if (!destination.startsWith(rootLocation)) {
            throw new IllegalArgumentException("Invalid file path.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        return storedName;
    }

    public Path load(String storedFileName) throws IOException {
        Path file = rootLocation.resolve(storedFileName).normalize();
        if (!file.startsWith(rootLocation) || !Files.exists(file)) {
            throw new IOException("Stored document file was not found.");
        }
        return file;
    }

    public void deleteQuietly(String storedFileName) {
        try {
            Path file = rootLocation.resolve(storedFileName).normalize();
            if (file.startsWith(rootLocation)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException ignored) {
            // Preserve the original exception if cleanup also fails.
        }
    }
}
