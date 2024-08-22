package tuneandmanner.wiselydiarybackend.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Map;
import java.util.UUID;
@Slf4j
@Service
public class SupabaseStorageService {

//    @Value("${supabase.url}")
    private final String supabaseUrl;

//    @Value("${supabase.key}")
    private final String supabaseKey;

//    @Value("${supabase.bucket}")
    private final String bucketName;

    private final RestTemplate restTemplate;

    public SupabaseStorageService(RestTemplate restTemplate, SupabaseProperties supabaseProperties) {
        this.restTemplate = restTemplate;
        this.supabaseUrl = supabaseProperties.getUrl();
        this.supabaseKey = supabaseProperties.getKey();
        this.bucketName = supabaseProperties.getBucket();
    }

    public String uploadImage(String localImagePath) {
        log.info("Attempting to upload image from path: {}", localImagePath);
        try {
            File imageFile = new File(localImagePath);
            if (!imageFile.exists()) {
                log.error("Image file does not exist: {}", localImagePath);
                throw new RuntimeException("Image file does not exist: " + localImagePath);
            }

            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getName();
            String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + fileName;
            log.debug("Upload URL: {}", uploadUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("apikey", supabaseKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(imageFile));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.debug("Sending request to Supabase");
            ResponseEntity<Map> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + fileName;
                log.info("Successfully uploaded image. Public URL: {}", publicUrl);
                return publicUrl;
            } else {
                log.error("Failed to upload image. Response status: {}, body: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to upload image to Supabase. Response: " + response);
            }
        } catch (Exception e) {
            log.error("Error uploading image to Supabase", e);
            throw new RuntimeException("Error uploading image to Supabase", e);
        }
    }
}
