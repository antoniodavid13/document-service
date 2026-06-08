package com.adfdev.document_service.service;

import com.adfdev.document_service.dto.response.ChunkResponse;
import com.adfdev.document_service.dto.response.DocumentResponse;
import com.adfdev.document_service.entity.Document;
import com.adfdev.document_service.entity.DocumentChunk;
import com.adfdev.document_service.entity.Subtopic;
import com.adfdev.document_service.entity.Topic;
import com.adfdev.document_service.exception.DocumentNotFoundException;
import com.adfdev.document_service.repository.DocumentChunkRepository;
import com.adfdev.document_service.repository.DocumentRepository;
import com.adfdev.document_service.repository.SubtopicRepository;
import com.adfdev.document_service.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final TopicRepository topicRepository;
    private final SubtopicRepository subtopicRepository;
    private final FileStorageService fileStorageService;
    private final PdfExtractorService pdfExtractorService;
    private final AiService aiService;

    @Transactional
    public DocumentResponse uploadDocument(String title, String subjectId, String topicId,
                                           String subtopicId, MultipartFile file) {
        // 1. Almacenar archivo
        String filename = fileStorageService.storeFile(file);

        // 2. Extraer texto del PDF
        Path filePath = fileStorageService.getFilePath(filename);
        String extractedText = pdfExtractorService.extractText(filePath);

        // 3. Generar resumen con IA
        String summary = aiService.generateSummary(extractedText);

        // 4. Guardar documento
        Document document = Document.builder()
                .title(title)
                .subjectId(subjectId)
                .topicId(topicId)
                .subtopicId(subtopicId)
                .fileUrl(filename)
                .summary(summary)
                .build();
        document = documentRepository.save(document);

        // 5. Crear chunks
        List<String> chunks = pdfExtractorService.splitIntoChunks(extractedText);
        String documentId = document.getId();
        for (String chunkContent : chunks) {
            DocumentChunk chunk = DocumentChunk.builder()
                    .documentId(documentId)
                    .content(chunkContent)
                    .embedding("[]")
                    .build();
            chunkRepository.save(chunk);
        }

        log.info("Documento procesado: {} ({} chunks)", title, chunks.size());
        return DocumentResponse.from(document);
    }

    public List<DocumentResponse> getDocumentsBySubject(String subjectId) {
        return documentRepository.findBySubjectId(subjectId).stream()
                .map(DocumentResponse::from)
                .toList();
    }

    public List<DocumentResponse> getDocumentsByTopic(String topicId) {
        return documentRepository.findByTopicId(topicId).stream()
                .map(DocumentResponse::from)
                .toList();
    }

    public DocumentResponse getDocumentById(String id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Documento no encontrado: " + id));
        return DocumentResponse.from(document);
    }

    public List<ChunkResponse> getChunksByDocument(String documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new DocumentNotFoundException("Documento no encontrado: " + documentId);
        }
        return chunkRepository.findByDocumentId(documentId).stream()
                .map(ChunkResponse::from)
                .toList();
    }

    // ── Topics ──────────────────────────────────────

    @Transactional
    public Topic createTopic(String subjectId, String name) {
        int count = topicRepository.findBySubjectIdOrderByOrderIndexAsc(subjectId).size();
        Topic topic = Topic.builder()
                .subjectId(subjectId)
                .name(name)
                .orderIndex(count)
                .build();
        topic = topicRepository.save(topic);
        log.info("Tema creado: {} en asignatura {}", name, subjectId);
        return topic;
    }

    public List<Map<String, Object>> getTopicsWithContent(String subjectId) {
        List<Topic> topics = topicRepository.findBySubjectIdOrderByOrderIndexAsc(subjectId);
        return topics.stream().map(topic -> {
            Map<String, Object> topicMap = new HashMap<>();
            topicMap.put("id", topic.getId());
            topicMap.put("name", topic.getName());
            topicMap.put("orderIndex", topic.getOrderIndex());

            List<Subtopic> subtopics = subtopicRepository.findByTopicIdOrderByOrderIndexAsc(topic.getId());
            List<Map<String, Object>> subtopicList = subtopics.stream().map(sub -> {
                Map<String, Object> subMap = new HashMap<>();
                subMap.put("id", sub.getId());
                subMap.put("name", sub.getName());
                subMap.put("orderIndex", sub.getOrderIndex());
                subMap.put("documents", documentRepository.findBySubtopicId(sub.getId()).stream()
                        .map(DocumentResponse::from).toList());
                return subMap;
            }).toList();

            topicMap.put("subtopics", subtopicList);
            topicMap.put("documents", documentRepository.findByTopicIdAndSubtopicIdIsNull(topic.getId()).stream()
                    .map(DocumentResponse::from).toList());
            return topicMap;
        }).toList();
    }

    @Transactional
    public void deleteTopic(String topicId) {
        documentRepository.findByTopicId(topicId).forEach(doc -> deleteDocument(doc.getId()));
        subtopicRepository.deleteByTopicId(topicId);
        topicRepository.deleteById(topicId);
        log.info("Tema eliminado: {}", topicId);
    }

    // ── Subtopics ───────────────────────────────────

    @Transactional
    public Subtopic createSubtopic(String topicId, String name) {
        int count = subtopicRepository.findByTopicIdOrderByOrderIndexAsc(topicId).size();
        Subtopic subtopic = Subtopic.builder()
                .topicId(topicId)
                .name(name)
                .orderIndex(count)
                .build();
        subtopic = subtopicRepository.save(subtopic);
        log.info("Subtema creado: {} en tema {}", name, topicId);
        return subtopic;
    }

    // ── Delete document ─────────────────────────────

    @Transactional
    public void deleteDocument(String id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Documento no encontrado: " + id));
        chunkRepository.deleteByDocumentId(id);
        fileStorageService.deleteFile(document.getFileUrl());
        documentRepository.delete(document);
        log.info("Documento eliminado: {}", id);
    }
}