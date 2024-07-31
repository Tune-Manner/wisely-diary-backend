package tuneandmanner.wiselydiarybackend.common.rag.letter;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class LetterDocsLoader {

    private static final Logger log = LoggerFactory.getLogger(LetterDocsLoader.class);
    private final JdbcTemplate jdbcTemplate;
    private final VectorStore vectorStore;

    @Value("classpath:/docs/letter-phrase.txt")
    private Resource txtPhraseResource;

    public LetterDocsLoader(JdbcTemplate jdbcTemplate, VectorStore vectorStore) {
        this.jdbcTemplate = jdbcTemplate;
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void init() {
        // 애플리케이션 시작 시 자동으로 벡터 스토어 업데이트 실행
        // 불필요 시 해당 실행 구문 제거 예정
        updateVectorStore();
    }

    public void updateVectorStore() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(txtPhraseResource.getInputStream(), StandardCharsets.UTF_8))) {
            List<Document> newDocuments = new ArrayList<>();
            String line;

            // 파일의 각 라인을 읽어 처리
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    // 이미 존재하는 문서인지 확인
                    if (!isDocumentExists(line)) {
                        // 새로운 문서라면 리스트에 추가
                        Document newDoc = new Document(line);
                        newDocuments.add(newDoc);
                    }
                }
            }

            // 새로운 문서가 있는 경우에만 처리
            if (!newDocuments.isEmpty()) {
                // 텍스트 분할기를 사용하여 문서 분할
                var textSplitter = new TokenTextSplitter();
                List<Document> splitDocuments = textSplitter.apply(newDocuments);

                // 분할된 문서를 벡터 스토어에 저장
                vectorStore.accept(splitDocuments);
                log.info("벡터 스토어에 {} 개의 새로운 문서가 추가되었습니다.", splitDocuments.size());
            } else {
                log.info("벡터 스토어에 추가할 새로운 문서가 없습니다.");
            }
        } catch (IOException e) {
            log.error("벡터 스토어 업데이트 중 오류 발생", e);
        }
    }

    private boolean isDocumentExists(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false; // 빈 내용은 존재하지 않는 것으로 처리
        }
        // 주어진 내용과 유사한 문서를 벡터 스토어에서 검색
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.query(content).withTopK(1));
        // 동일한 내용의 문서가 이미 존재하는지 확인
        return !similarDocuments.isEmpty() && similarDocuments.get(0).getContent().equals(content);
    }
}
