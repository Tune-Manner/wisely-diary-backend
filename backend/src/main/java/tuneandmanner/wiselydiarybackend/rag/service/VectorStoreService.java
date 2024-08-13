package tuneandmanner.wiselydiarybackend.rag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import tuneandmanner.wiselydiarybackend.common.vectorstore.CustomPgVectorStore;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 벡터 저장소 관련 기능을 제공하는 서비스 클래스입니다.
 * 이 클래스는 요약, 편지, 음악에 대한 벡터 저장소를 관리합니다.
 */
@Service
public class VectorStoreService {

    private static final Logger logger = LoggerFactory.getLogger(VectorStoreService.class);

    private final PgVectorStore pgVectorStoreSummary;
    private final PgVectorStore pgVectorStoreLetter;
    private final PgVectorStore pgVectorStoreMusic;

    /**
     * VectorStoreService의 생성자입니다.
     * @param pgVectorStoreSummary 요약을 위한 벡터 저장소
     * @param pgVectorStoreLetter 편지를 위한 벡터 저장소
     * @param pgVectorStoreMusic 음악을 위한 벡터 저장소
     */
    public VectorStoreService(@Qualifier("pgVectorStoreSummary") PgVectorStore pgVectorStoreSummary,
                              @Qualifier("pgVectorStoreLetter") PgVectorStore pgVectorStoreLetter,
                              @Qualifier("pgVectorStoreMusic") PgVectorStore pgVectorStoreMusic
    ) {
        this.pgVectorStoreSummary = pgVectorStoreSummary;
        this.pgVectorStoreLetter = pgVectorStoreLetter;
        this.pgVectorStoreMusic = pgVectorStoreMusic;
        logger.info("VectorStoreService initialized with summary, letter, and music stores.");
    }

    /**
     * 주어진 쿼리와 유사한 문서를 검색합니다.
     * @param query 검색 쿼리
     * @param storeType 검색할 저장소 유형 ("summary", "letter", "music")
     * @return 유사한 문서의 내용 리스트
     */
    public List<String> searchSimilarDocuments(String query, String storeType) {
        logger.info("Searching similar documents for query: '{}' in store type: {}", query, storeType);
        PgVectorStore store = getStore(storeType);
        SearchRequest searchRequest = SearchRequest.query(query).withTopK(1);
        List<Document> results = store.similaritySearch(searchRequest);
        logger.debug("Found {} similar documents", results.size());
        List<String> contents = results.stream()
                .map(Document::getContent)
                .collect(Collectors.toList());
        logger.info("Completed search for query: '{}' in store type: {}", query, storeType);
        return contents;
    }

    /**
     * 새로운 문서를 벡터 저장소에 추가합니다.
     * @param content 문서 내용
     * @param metadata 문서 메타데이터
     * @param storeType 저장할 저장소 유형 ("summary", "letter", "music")
     */
    public void addDocument(String content, Map<String, Object> metadata, String storeType) {
        logger.info("Adding new document to store type: {}", storeType);
        logger.debug("Document content: {}", content);
        logger.debug("Document metadata: {}", metadata);
        PgVectorStore store = getStore(storeType);
        Document document = new Document(content, metadata);
        store.add(List.of(document));
        logger.info("Document added successfully to store type: {}", storeType);
    }

    /**
     * 텍스트 파일의 내용을 문서로 변환하여 벡터 저장소에 추가합니다.
     * @param content 텍스트 파일 내용
     * @param fileName 파일 이름
     * @param storeType 저장할 저장소 유형 ("summary", "letter", "music")
     */
    public String addDocumentFromText(String content, String fileName, String storeType) {
        logger.info("Processing document from file: {} for store type: {}", fileName, storeType);

        // 중복 및 유사도 검사
        if (isDuplicateOrSimilarDocument(content, storeType)) {
            logger.info("Document from file: {} is duplicate or similar. Skipping upload.", fileName);
            return "Document already exists or is very similar to an existing document. Skipping upload.";
        }

        List<Document> documents = splitDocumentLineByLine(content, fileName);
        logger.debug("Split text into {} documents", documents.size());
        PgVectorStore store = getStore(storeType);

        int addedCount = 0;
        if (store instanceof CustomPgVectorStore) {
            CustomPgVectorStore customStore = (CustomPgVectorStore) store;
            for (Document doc : documents) {
                if (!customStore.isDocumentExists(calculateContentHash(doc.getContent())) &&
                        !customStore.isDocumentSimilar(customStore.getEmbeddingModel().embed(doc.getContent()))) {
                    customStore.add(Collections.singletonList(doc));
                    addedCount++;
                }
            }
        } else {
            store.add(documents);
            addedCount = documents.size();
        }

        logger.info("Added {} new documents from file: {} to store type: {}", addedCount, fileName, storeType);
        return String.format("Added %d new documents from file: %s", addedCount, fileName);
    }

    /**
     * 텍스트 내용을 줄 단위로 분할하여 Document 객체 리스트로 변환합니다.
     * @param content 텍스트 내용
     * @param fileName 파일 이름 (메타데이터로 사용)
     * @return Document 객체 리스트
     */
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
                .collect(Collectors.toList());
        logger.debug("Split {} lines into {} documents", lines.length, documents.size());
        return documents;
    }

    /**
     * 주어진 저장소 유형에 해당하는 PgVectorStore 객체를 반환합니다.
     * @param storeType 저장소 유형 ("summary", "letter", "music")
     * @return 해당 유형의 PgVectorStore 객체
     * @throws IllegalArgumentException 유효하지 않은 저장소 유형이 주어졌을 때 발생
     */
    private PgVectorStore getStore(String storeType) {
        if (storeType == null) {
            logger.error("Null store type received");
            throw new IllegalArgumentException("Store type cannot be null");
        }

        String trimmedStoreType = storeType.trim().toLowerCase();
        logger.debug("Getting store for type: '{}'", trimmedStoreType);

        PgVectorStore store = switch (trimmedStoreType) {
            case "summary" -> pgVectorStoreSummary;
            case "letter" -> pgVectorStoreLetter;
            case "music" -> pgVectorStoreMusic;
            default -> {
                logger.error("Invalid store type requested: '{}'", trimmedStoreType);
                throw new IllegalArgumentException("Invalid store type: " + trimmedStoreType);
            }
        };

        logger.debug("Retrieved store for type: '{}'", trimmedStoreType);
        return store;
    }
    public boolean isDuplicateOrSimilarDocument(String content, String storeType) {
        logger.debug("Checking for duplicate or similar document. Content length: {}, StoreType: {}", content.length(), storeType);

        String contentHash = calculateContentHash(content);
        logger.debug("Calculated content hash: {}", contentHash);

        PgVectorStore store = getStore(storeType);
        logger.debug("Retrieved store for type: {}", storeType);

        if (store instanceof CustomPgVectorStore) {
            CustomPgVectorStore customStore = (CustomPgVectorStore) store;

            boolean exists = customStore.isDocumentExists(contentHash);
            logger.debug("Document exists check result: {}", exists);

            if (exists) {
                return true;
            }

            List<List<Double>> embeddings = new ArrayList<>();
            List<String> chunks = splitContentIntoChunks(content);

            for (String chunk : chunks) {
                try {
                    List<Double> embedding = customStore.getEmbeddingModel().embed(chunk);
                    embeddings.add(embedding);
                } catch (Exception e) {
                    logger.warn("Error embedding chunk: {}", e.getMessage());
                    // 오류가 발생한 청크는 건너뜁니다.
                }
            }

            logger.debug("Generated embeddings for {} chunks", embeddings.size());

            if (embeddings.isEmpty()) {
                logger.warn("No valid embeddings generated for content");
                return false;
            }

            // 여러 임베딩의 평균을 계산합니다.
            List<Double> averageEmbedding = calculateAverageEmbedding(embeddings);

            boolean isSimilar = customStore.isDocumentSimilar(averageEmbedding);
            logger.debug("Document similarity check result: {}", isSimilar);

            return isSimilar;
        }

        logger.warn("Store is not an instance of CustomPgVectorStore. Store type: {}", store.getClass().getName());
        return false;
    }

    private List<String> splitContentIntoChunks(String content) {
        // 이 메소드는 컨텐츠를 적절한 크기의 청크로 나눕니다.
        // 예를 들어, 단어 수나 문자 수를 기준으로 나눌 수 있습니다.
        // 여기서는 간단한 예시로 1000자 단위로 나누었습니다.
        List<String> chunks = new ArrayList<>();
        int chunkSize = 1000;
        for (int i = 0; i < content.length(); i += chunkSize) {
            chunks.add(content.substring(i, Math.min(content.length(), i + chunkSize)));
        }
        return chunks;
    }

    private List<Double> calculateAverageEmbedding(List<List<Double>> embeddings) {
        int dimensions = embeddings.get(0).size();
        List<Double> average = new ArrayList<>(Collections.nCopies(dimensions, 0.0));

        for (List<Double> embedding : embeddings) {
            for (int i = 0; i < dimensions; i++) {
                average.set(i, average.get(i) + embedding.get(i));
            }
        }

        for (int i = 0; i < dimensions; i++) {
            average.set(i, average.get(i) / embeddings.size());
        }

        return average;
    }


    private String calculateContentHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            String result = Base64.getEncoder().encodeToString(hash);
            logger.debug("Calculated content hash: {}", result);
            return result;
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error calculating content hash", e);
            throw new RuntimeException("Error calculating content hash", e);
        }
    }
}