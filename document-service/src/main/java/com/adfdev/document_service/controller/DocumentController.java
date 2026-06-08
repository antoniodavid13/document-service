package com.adfdev.document_service.controller;

import com.adfdev.document_service.dto.response.ChunkResponse;
import com.adfdev.document_service.dto.response.DocumentResponse;
import com.adfdev.document_service.entity.Subtopic;
import com.adfdev.document_service.entity.Topic;
import com.adfdev.document_service.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    // ── Documents ───────────────────────────────────

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("title") String title,
            @RequestParam("subjectId") String subjectId,
            @RequestParam(value = "topicId", required = false) String topicId,
            @RequestParam(value = "subtopicId", required = false) String subtopicId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.uploadDocument(title, subjectId, topicId, subtopicId, file));
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsBySubject(
            @PathVariable String subjectId) {
        return ResponseEntity.ok(documentService.getDocumentsBySubject(subjectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(@PathVariable String id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @GetMapping("/{id}/chunks")
    public ResponseEntity<List<ChunkResponse>> getChunks(@PathVariable String id) {
        return ResponseEntity.ok(documentService.getChunksByDocument(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    // ── Topics ──────────────────────────────────────

    @PostMapping("/topics")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Topic> createTopic(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.createTopic(body.get("subjectId"), body.get("name")));
    }

    @GetMapping("/topics/subject/{subjectId}")
    public ResponseEntity<List<Map<String, Object>>> getTopics(@PathVariable String subjectId) {
        return ResponseEntity.ok(documentService.getTopicsWithContent(subjectId));
    }

    @DeleteMapping("/topics/{topicId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Void> deleteTopic(@PathVariable String topicId) {
        documentService.deleteTopic(topicId);
        return ResponseEntity.noContent().build();
    }

    // ── Subtopics ───────────────────────────────────

    @PostMapping("/subtopics")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Subtopic> createSubtopic(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.createSubtopic(body.get("topicId"), body.get("name")));
    }
    @GetMapping("/{id}/file")
    public ResponseEntity<org.springframework.core.io.Resource> getFile(@PathVariable String id) {
        var doc = documentService.getDocumentById(id);
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get("./uploads").toAbsolutePath().normalize().resolve(doc.getFileUrl());
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header("Content-Type", "application/pdf")
                        .header("Content-Disposition", "inline; filename=\"" + doc.getTitle() + ".pdf\"")
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}