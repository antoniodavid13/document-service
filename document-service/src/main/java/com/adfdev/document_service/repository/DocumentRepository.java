package com.adfdev.document_service.repository;

import com.adfdev.document_service.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

    List<Document> findBySubjectId(String subjectId);

    List<Document> findByTopicId(String topicId);

    List<Document> findBySubtopicId(String subtopicId);

    List<Document> findByTopicIdAndSubtopicIdIsNull(String topicId);
}