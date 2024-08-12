package tuneandmanner.wiselydiarybackend.rag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import tuneandmanner.wiselydiarybackend.common.vectorstore.CustomPgVectorStore;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class VectorStoreService {

    private static final Logger logger = LoggerFactory.getLogger(VectorStoreService.class);

    private final Map<String, CustomPgVectorStore> vectorStores;
    private final EmbeddingModel embeddingModel;

    public VectorStoreService(
            @Qualifier("pgVectorStoreSummary") CustomPgVectorStore summaryStore,
            @Qualifier("pgVectorStoreLetter") CustomPgVectorStore letterStore,
            @Qualifier("pgVectorStoreMusic") CustomPgVectorStore musicStore,
            EmbeddingModel embeddingModel
    ) {
        this.vectorStores = new HashMap<>();
        this.vectorStores.put("summary", summaryStore);
        this.vectorStores.put("letter", letterStore);
        this.vectorStores.put("music", musicStore);
        this.embeddingModel = embeddingModel;
        logger.info("VectorStoreService initialized with stores: {}", String.join(", ", vectorStores.keySet()));
    }

    public List<String> getAvailableStoreTypes() {
        return new ArrayList<>(vectorStores.keySet());
    }

    public List<String> searchSimilarDocuments(String query, String storeType) {
        logger.info("Searching similar documents for query: '{}' in store type: {}", query, storeType);
        CustomPgVectorStore store = getStore(storeType);
        SearchRequest searchRequest = SearchRequest.query(query).withTopK(5);
        List<Document> results = store.similaritySearch(searchRequest);
        logger.debug("Found {} similar documents", results.size());
        List<String> contents = results.stream()
                .map(Document::getContent)
                .toList();
        logger.info("Completed search for query: '{}' in store type: {}", query, storeType);
        return contents;
    }

    @Transactional
    public String addDocument(String content, Map<String, Object> metadata, String storeType) {
        logger.debug("Adding new document to store type: {}. Content length: {}", storeType, content.length());
        CustomPgVectorStore store = getStore(storeType);
        Document document = new Document(content, metadata);
        store.add(List.of(document));
        logger.info("Document added successfully to store type: {}", storeType);
        return "Document added successfully";
    }

    public CompletableFuture<String> addDocumentAsync(String content, Map<String, Object> metadata, String storeType) {
        logger.debug("Asynchronously adding new document to store type: {}. Content length: {}", storeType, content.length());
        CustomPgVectorStore store = getStore(storeType);

        List<Document> documents = Arrays.stream(content.split("\n"))
                .filter(line -> !line.trim().isEmpty())
                .map(line -> new Document(line.trim(), new HashMap<>(metadata)))
                .collect(Collectors.toList());

        logger.debug("Split content into {} documents", documents.size());

        return store.addAsync(documents)
                .thenApply(v -> {
                    logger.info("Documents added asynchronously to store type: {}", storeType);
                    return String.format("Added documents successfully");
                })
                .exceptionally(e -> {
                    logger.error("Error adding documents asynchronously to store type: {}", storeType, e);
                    return "Error adding documents: " + e.getMessage();
                });
    }

    public String addDocumentFromText(String content, String fileName, String storeType) {
        logger.info("Processing document from file: {} for store type: {}", fileName, storeType);

        if (isDuplicateOrSimilarDocument(content, storeType)) {
            logger.info("Document from file: {} is duplicate or similar. Skipping upload.", fileName);
            return "Document already exists or is very similar to an existing document. Skipping upload.";
        }

        CustomPgVectorStore store = getStore(storeType);
        List<Document> documents = splitDocumentLineByLine(content, fileName);
        logger.debug("Split text into {} documents", documents.size());

        int addedCount = 0;
        for (Document doc : documents) {
            List<Double> embedding = embeddingModel.embed(doc.getContent());
            if (!store.isDocumentExists(calculateContentHash(doc.getContent())) &&
                    !store.isDocumentSimilar(embedding)) {
                store.add(Collections.singletonList(doc));
                addedCount++;
            }
        }

        logger.info("Added {} new documents from file: {} to store type: {}", addedCount, fileName, storeType);
        return String.format("Added %d new documents from file: %s", addedCount, fileName);
    }

    private List<Document> splitDocumentLineByLine(String content, String fileName) {
        logger.debug("Splitting document content into lines for file: {}", fileName);
        String[] lines = content.split("\n");
        List<Document> documents = Arrays.stream(lines)
                .filter(line -> !line.trim().isEmpty())
                .map(line -> {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("fileName", fileName);
                    return new Document(line.trim(), metadata);
                })
                .toList();
        logger.debug("Split {} lines into {} documents", lines.length, documents.size());
        return documents;
    }

    private CustomPgVectorStore getStore(String storeType) {
        CustomPgVectorStore store = vectorStores.get(storeType);
        if (store == null) {
            logger.error("Invalid store type requested: '{}'", storeType);
            throw new IllegalArgumentException("Invalid store type: " + storeType);
        }
        return store;
    }

    public boolean isDuplicateOrSimilarDocument(String content, String storeType) {
        logger.debug("Checking for duplicate or similar document. Content length: {}, StoreType: {}", content.length(), storeType);

        String contentHash = calculateContentHash(content);
        logger.debug("Calculated content hash: {}", contentHash);

        CustomPgVectorStore store = getStore(storeType);

        boolean exists = store.isDocumentExists(contentHash);
        logger.debug("Document exists check result: {}", exists);

        if (exists) {
            return true;
        }

        List<Double> embedding = embeddingModel.embed(content);
        boolean isSimilar = store.isDocumentSimilar(embedding);
        logger.debug("Document similarity check result: {}", isSimilar);

        return isSimilar;
    }

    private String calculateContentHash(String content) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String result = java.util.Base64.getEncoder().encodeToString(hash);
            logger.debug("Calculated content hash: {}", result);
            return result;
        } catch (java.security.NoSuchAlgorithmException e) {
            logger.error("Error calculating content hash", e);
            throw new RuntimeException("Error calculating content hash", e);
        }
    }
}