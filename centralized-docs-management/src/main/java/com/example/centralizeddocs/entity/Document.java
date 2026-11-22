package com.example.centralizeddocs.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "documents")
public class Document {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String customerId;

	@Column(length = 50)
	private String accountNumber;

	@Column(nullable = false, length = 20)
	private String repositoryCode;

	@Column(nullable = false, length = 100)
	private String documentType;

	@Column(length = 255)
	private String description;

	@Column(nullable = false, length = 100)
	private String documentCategory;

	@Column(length = 100)
	private String subCategory;

	@Column(nullable = false, length = 30)
	private String formStatus;

	@Column(nullable = false, length = 255)
	private String originalFileName;

	@Column(nullable = false, unique = true, length = 100)
	private String storedFileName;

	@Column(nullable = false, length = 150)
	private String contentType;

	@Column(nullable = false)
	private LocalDateTime uploadedAt;

	@Column(nullable = false)
	private LocalDate retentionUntil;

	@Column(nullable = false)
	private boolean archived = false;

	@Column(nullable = false)
	private boolean deleted = false;

	@PrePersist
	public void beforeInsert() {
		if (uploadedAt == null) {
			uploadedAt = LocalDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getRepositoryCode() {
		return repositoryCode;
	}

	public void setRepositoryCode(String repositoryCode) {
		this.repositoryCode = repositoryCode;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDocumentCategory() {
		return documentCategory;
	}

	public void setDocumentCategory(String documentCategory) {
		this.documentCategory = documentCategory;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	public String getFormStatus() {
		return formStatus;
	}

	public void setFormStatus(String formStatus) {
		this.formStatus = formStatus;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	public String getStoredFileName() {
		return storedFileName;
	}

	public void setStoredFileName(String storedFileName) {
		this.storedFileName = storedFileName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}

	public void setUploadedAt(LocalDateTime uploadedAt) {
		this.uploadedAt = uploadedAt;
	}

	public LocalDate getRetentionUntil() {
		return retentionUntil;
	}

	public void setRetentionUntil(LocalDate retentionUntil) {
		this.retentionUntil = retentionUntil;
	}

	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}
