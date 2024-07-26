package tuneandmanner.wiselydiarybackend.stt.util;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileSplitter {

	private static final long CHUNK_SIZE = 25 * 1024 * 1024; // 25MB

	public static List<File> splitFile(MultipartFile file) throws IOException {
		List<File> chunks = new ArrayList<>();
		byte[] fileContent = file.getBytes();
		int numChunks = (int) Math.ceil((double) fileContent.length / CHUNK_SIZE);

		for (int i = 0; i < numChunks; i++) {
			int start = (int) (i * CHUNK_SIZE);
			int end = Math.min(fileContent.length, (int) ((i + 1) * CHUNK_SIZE));
			File chunk = File.createTempFile("chunk" + i, ".tmp");
			FileUtils.writeByteArrayToFile(chunk, subArray(fileContent, start, end));
			chunks.add(chunk);
		}
		return chunks;
	}

	private static byte[] subArray(byte[] array, int start, int end) {
		byte[] result = new byte[end - start];
		System.arraycopy(array, start, result, 0, end - start);
		return result;
	}
}
