package com.adfdev.document_service.repository;

import com.adfdev.document_service.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, String> {
    List<Topic> findBySubjectIdOrderByOrderIndexAsc(String subjectId);
}