package com.adfdev.document_service.dto.response;

import com.adfdev.document_service.entity.DocumentChunk;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkResponse {

    private String id;
    private String documentId;
    private String content;

    public static ChunkResponse from(DocumentChunk chunk) {
        return ChunkResponse.builder()
                .id(chunk.getId())
                .documentId(chunk.getDocumentId())
                .content(chunk.getContent())
                .build();
    }
}