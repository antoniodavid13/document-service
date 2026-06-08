package com.adfdev.document_service.dto.response;

import com.adfdev.document_service.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private String id;
    private String subjectId;
    private String topicId;
    private String subtopicId;
    private String title;
    private String fileUrl;
    private String summary;
    private LocalDateTime uploadedAt;

    public static DocumentResponse from(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .subjectId(document.getSubjectId())
                .topicId(document.getTopicId())
                .subtopicId(document.getSubtopicId())
                .title(document.getTitle())
                .fileUrl(document.getFileUrl())
                .summary(document.getSummary())
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}