package com.adfdev.document_service.repository;

import com.adfdev.document_service.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, String> {

    List<DocumentChunk> findByDocumentId(String documentId);

    void deleteByDocumentId(String documentId);
}