package tuneandmanner.wiselydiarybackend.rag.service;

import org.springframework.stereotype.Service;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class VectorStoreService {

    private final PgVectorStore pgVectorStoreSummary;
    private final PgVectorStore pgVectorStoreLetter;
    private final PgVectorStore pgVectorStoreMusic;

    public VectorStoreService(@Qualifier("pgVectorStoreSummary") PgVectorStore pgVectorStoreSummary,
                              @Qualifier("pgVectorStoreLetter") PgVectorStore pgVectorStoreLetter,
                              @Qualifier("pgVectorStoreMusic") PgVectorStore pgVectorStoreMusic
    ) {
        this.pgVectorStoreSummary = pgVectorStoreSummary;
        this.pgVectorStoreLetter = pgVectorStoreLetter;
        this.pgVectorStoreMusic = pgVectorStoreMusic;
    }

    public List<String> searchSimilarDocuments(String query, String storeType) {
        PgVectorStore store = getStore(storeType);
        SearchRequest searchRequest = SearchRequest.query(query).withTopK(5);
        List<Document> results = store.similaritySearch(searchRequest);
        return results.stream()
                .map(Document::getContent)
                .collect(Collectors.toList());
    }

    public void addDocument(String content, Map<String, Object> metadata, String storeType) {
        PgVectorStore store = getStore(storeType);
        Document document = new Document(content, metadata);
        store.add(List.of(document));
    }

    public void addDocumentFromText(String content, String fileName, String storeType) {
        List<Document> documents = splitDocumentLineByLine(content, fileName);
        PgVectorStore store = getStore(storeType);
        store.add(documents);
    }

    private List<Document> splitDocumentLineByLine(String content, String fileName) {
        String[] lines = content.split("\n");
        return Arrays.stream(lines)
                .filter(line -> !line.trim().isEmpty())
                .map(line -> {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("fileName", fileName);
                    return new Document(line.trim(), metadata);
                })
                .collect(Collectors.toList());
    }

    private PgVectorStore getStore(String storeType) {
        return switch (storeType) {
            case "summary" -> pgVectorStoreSummary;
            case "letter" -> pgVectorStoreLetter;
            case "music" -> pgVectorStoreMusic;
            default -> throw new IllegalArgumentException("Invalid store type: " + storeType);
        };
    }
}