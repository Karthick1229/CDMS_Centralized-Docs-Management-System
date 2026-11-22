package com.example.centralizeddocs.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.centralizeddocs.entity.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {

	Optional<Document> findByIdAndDeletedFalse(Long id);

	@Query("select d from Document d where d.deleted = false "
			+ "and (:customerId is null or lower(d.customerId) like lower(concat('%', :customerId, '%'))) "
			+ "and (:accountNumber is null or lower(d.accountNumber) like lower(concat('%', :accountNumber, '%'))) "
			+ "and (:documentType is null or lower(d.documentType) like lower(concat('%', :documentType, '%'))) "
			+ "and (:repositoryCode is null or lower(d.repositoryCode) = lower(:repositoryCode)) "
			+ "and (:documentCategory is null or lower(d.documentCategory) like lower(concat('%', :documentCategory, '%'))) "
			+ "order by d.uploadedAt desc")
	List<Document> search(@Param("customerId") String customerId, @Param("accountNumber") String accountNumber,
			@Param("documentType") String documentType, @Param("repositoryCode") String repositoryCode,
			@Param("documentCategory") String documentCategory);

	List<Document> findByArchivedFalseAndDeletedFalseAndRetentionUntilLessThanEqual(LocalDate date);
}
