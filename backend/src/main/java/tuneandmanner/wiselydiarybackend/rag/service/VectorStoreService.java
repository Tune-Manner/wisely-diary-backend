package tuneandmanner.wiselydiarybackend.rag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;

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
        SearchRequest searchRequest = SearchRequest.query(query).withTopK(5);
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
    public void addDocumentFromText(String content, String fileName, String storeType) {
        logger.info("Adding document from text file: {} to store type: {}", fileName, storeType);
        List<Document> documents = splitDocumentLineByLine(content, fileName);
        logger.debug("Split text into {} documents", documents.size());
        PgVectorStore store = getStore(storeType);
        store.add(documents);
        logger.info("Added {} documents from file: {} to store type: {}", documents.size(), fileName, storeType);
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
        logger.debug("Getting store for type: {}", storeType);
        PgVectorStore store = switch (storeType) {
            case "summary" -> pgVectorStoreSummary;
            case "letter" -> pgVectorStoreLetter;
            case "music" -> pgVectorStoreMusic;
            default -> {
                logger.error("Invalid store type requested: {}", storeType);
                throw new IllegalArgumentException("Invalid store type: " + storeType);
            }
        };
        logger.debug("Retrieved store for type: {}", storeType);
        return store;
    }
}