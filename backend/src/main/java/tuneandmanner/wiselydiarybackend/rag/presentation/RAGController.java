package tuneandmanner.wiselydiarybackend.rag.presentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tuneandmanner.wiselydiarybackend.common.exception.dto.response.ExceptionResponse;
import tuneandmanner.wiselydiarybackend.common.vectorstore.CustomPgVectorStore;
import tuneandmanner.wiselydiarybackend.rag.service.RAGService;
import tuneandmanner.wiselydiarybackend.rag.service.VectorStoreService;
import tuneandmanner.wiselydiarybackend.common.exception.DocumentUploadException;
import tuneandmanner.wiselydiarybackend.common.exception.type.ExceptionCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/rag")
public class RAGController {

    private static final Logger logger = LoggerFactory.getLogger(RAGController.class);

    private final RAGService ragService;
    private final VectorStoreService vectorStoreService;

    public RAGController(RAGService ragService, VectorStoreService vectorStoreService) {
        this.ragService = ragService;
        this.vectorStoreService = vectorStoreService;
        logger.info("RAGController initialized with RAGService and VectorStoreService");
    }

    @PostMapping("/upload")
    public CompletableFuture<ResponseEntity<String>> uploadDocument(@RequestParam("file") MultipartFile file, @RequestParam("storeType") String storeType) {
        logger.info("Received document upload request for store type: {}", storeType);

        if (!vectorStoreService.getAvailableStoreTypes().contains(storeType)) {
            logger.error("Invalid store type: {}", storeType);
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body("Invalid store type: " + storeType));
        }

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String fileName = file.getOriginalFilename();
            logger.debug("File name: {}, Content length: {}", fileName, content.length());

            return vectorStoreService.addDocumentAsync(content, Map.of("fileName", fileName), storeType)
                    .thenApply(result -> {
                        logger.info("Document upload result: {}", result);
                        return ResponseEntity.ok(result);
                    })
                    .exceptionally(e -> {
                        logger.error("Error during document upload", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during upload: " + e.getMessage());
                    });
        } catch (IOException e) {
            logger.error("Error reading file content", e);
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error reading file content"));
        }
    }

    @PostMapping("/query")
    public ResponseEntity<Map<String, String>> query(@RequestParam("query") String query,
                                                     @RequestParam("context") String context,
                                                     @RequestParam("storeType") String storeType) {
        logger.info("Received query request: '{}', store type: {}", query, storeType);

        if (!vectorStoreService.getAvailableStoreTypes().contains(storeType)) {
            logger.error("Invalid store type: {}", storeType);
            throw new IllegalArgumentException("Invalid store type: " + storeType);
        }

        Map<String, String> response = ragService.generateResponse(query, context, storeType);
        logger.info("Generated response for query: '{}'", query);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(DocumentUploadException.class)
    public ResponseEntity<ExceptionResponse> handleDocumentUploadException(DocumentUploadException e) {
        final ExceptionResponse exceptionResponse = ExceptionResponse.of(e.getCode(), e.getMessage());
        return ResponseEntity.badRequest().body(exceptionResponse);
    }
}