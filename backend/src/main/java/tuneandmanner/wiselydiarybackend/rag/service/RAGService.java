package tuneandmanner.wiselydiarybackend.rag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tuneandmanner.wiselydiarybackend.common.vectorstore.CustomPgVectorStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RAGService {

    private static final Logger logger = LoggerFactory.getLogger(RAGService.class);

    private final Map<String, CustomPgVectorStore> vectorStores;
    private final ChatModel chatModel;
    private final PromptTemplate ragPromptTemplate;

    public RAGService(
            @Qualifier("pgVectorStoreSummary") CustomPgVectorStore summaryStore,
            @Qualifier("pgVectorStoreLetter") CustomPgVectorStore letterStore,
            @Qualifier("pgVectorStoreMusic") CustomPgVectorStore musicStore,
            ChatModel chatModel,
            @Qualifier("ragPromptTemplate") PromptTemplate ragPromptTemplate
    ) {
        this.vectorStores = new HashMap<>();
        this.vectorStores.put("summary", summaryStore);
        this.vectorStores.put("letter", letterStore);
        this.vectorStores.put("music", musicStore);
        this.chatModel = chatModel;
        this.ragPromptTemplate = ragPromptTemplate;
        logger.info("RAGService initialized with vector stores for summary, letter, and music");
    }

    public Map<String, String> generateResponse(String query, String context, String storeType) {
        logger.info("Generating response for query: '{}', using store type: {}", query, storeType);

        CustomPgVectorStore store = vectorStores.get(storeType);
        if (store == null) {
            logger.error("Invalid store type: {}", storeType);
            throw new IllegalArgumentException("Invalid store type: " + storeType);
        }

        List<Document> similarDocuments = searchSimilarDocuments(query, store);
        logger.debug("Found {} similar documents", similarDocuments.size());

        Prompt prompt = createPrompt(query, context, similarDocuments);
        logger.debug("Created prompt: {}", prompt.getContents());

        ChatResponse chatResponse = chatModel.call(prompt);
        String response = chatResponse.getResult().getOutput().getContent();
        logger.info("Generated response: {}", response);

        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        result.put("similarDocuments", formatSimilarDocuments(similarDocuments));

        return result;
    }

    private List<Document> searchSimilarDocuments(String query, CustomPgVectorStore store) {
        logger.debug("Searching for documents similar to query: '{}'", query);
        SearchRequest searchRequest = SearchRequest.query(query).withTopK(5);
        return store.similaritySearch(searchRequest);
    }

    private Prompt createPrompt(String query, String context, List<Document> similarDocuments) {
        String combinedContext = formatSimilarDocuments(similarDocuments) + "\n" + context;
        Map<String, Object> model = new HashMap<>();
        model.put("context", combinedContext);
        model.put("query", query);
        return new Prompt(ragPromptTemplate.render(model));
    }

    private String formatSimilarDocuments(List<Document> documents) {
        return documents.stream()
                .map(doc -> "Content: " + doc.getContent() + "\nMetadata: " + doc.getMetadata())
                .collect(Collectors.joining("\n\n"));
    }
}