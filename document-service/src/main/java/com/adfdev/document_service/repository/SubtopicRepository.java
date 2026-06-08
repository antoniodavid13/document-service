package com.adfdev.document_service.repository;

import com.adfdev.document_service.entity.Subtopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubtopicRepository extends JpaRepository<Subtopic, String> {
    List<Subtopic> findByTopicIdOrderByOrderIndexAsc(String topicId);
    void deleteByTopicId(String topicId);
}