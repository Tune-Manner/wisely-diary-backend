package tuneandmanner.wiselydiarybackend.stt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import tuneandmanner.wiselydiarybackend.auth.config.OpenAIClientConfig;
import tuneandmanner.wiselydiarybackend.stt.client.OpenAIClient;
import tuneandmanner.wiselydiarybackend.stt.dto.WhisperTranscriptionResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class OpenAIClientService {

	private static final Logger logger = LoggerFactory.getLogger(OpenAIClientService.class);

	private final OpenAIClient openAIClient;
	private final OpenAIClientConfig openAIClientConfig;

	public OpenAIClientService(OpenAIClient openAIClient, OpenAIClientConfig openAIClientConfig) {
		this.openAIClient = openAIClient;
		this.openAIClientConfig = openAIClientConfig;
	}

	public WhisperTranscriptionResponse createTranscription(FileSystemResource fileResource) throws IOException {
		String model = openAIClientConfig.getAudioModel();
		logger.info("Calling OpenAI API with model: {}", model);
		logger.info("File name: {}, size: {} bytes", fileResource.getFilename(), fileResource.contentLength());

		try {
			MultipartFile multipartFile = new MultipartFile() {
				@Override
				public String getName() {
					return fileResource.getFilename();
				}

				@Override
				public String getOriginalFilename() {
					return fileResource.getFilename();
				}

				@Override
				public String getContentType() {
					return "audio/wav"; // 또는 적절한 MIME 타입
				}

				@Override
				public boolean isEmpty() {
                    try {
                        return fileResource.contentLength() == 0;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

				@Override
				public long getSize() {
                    try {
                        return fileResource.contentLength();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

				@Override
				public byte[] getBytes() throws IOException {
					return fileResource.getInputStream().readAllBytes();
				}

				@Override
				public InputStream getInputStream() throws IOException {
					return fileResource.getInputStream();
				}

				@Override
				public Resource getResource() {
					return fileResource;
				}

				@Override
				public void transferTo(File dest) throws IOException, IllegalStateException {
					try (FileOutputStream fos = new FileOutputStream(dest)) {
						fos.write(getBytes());
					}
				}
			};

			WhisperTranscriptionResponse response = openAIClient.createTranscription(multipartFile, model);
			logger.info("Transcription completed successfully");

			// 트랜스크립션 결과 로깅
			logger.info("Transcription result: {}", response.getText());

			return response;
		} catch (Exception e) {
			logger.error("Error during transcription: ", e);
			throw e;
		}
	}

	// MultipartFile을 FileSystemResource로 변환하는 유틸리티 메서드
	public FileSystemResource convertMultipartFileToFileSystemResource(MultipartFile file) throws IOException {
		File tempFile = File.createTempFile("temp", file.getOriginalFilename());
		file.transferTo(tempFile);
		return new FileSystemResource(tempFile);
	}
}
