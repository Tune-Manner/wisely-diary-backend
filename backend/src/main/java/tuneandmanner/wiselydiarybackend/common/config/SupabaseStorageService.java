package tuneandmanner.wiselydiarybackend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Map;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucketName;

    private final RestTemplate restTemplate;

    public SupabaseStorageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String uploadImage(String localImagePath) {
        try {
            File imageFile = new File(localImagePath);
            if (!imageFile.exists()) {
                throw new RuntimeException("Image file does not exist: " + localImagePath);
            }

            String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + imageFile.getName();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("apikey", supabaseKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            System.out.println("도도"+supabaseKey+bucketName+supabaseUrl);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(imageFile));
            System.out.println("레레");
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            System.out.println("미미");
            ResponseEntity<Map> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );
            System.out.println("파파");
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                System.out.println("솔솔");
                String uploadedFilePath = (String) response.getBody().get("Key");
                System.out.println("라라");
                return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + uploadedFilePath;
            } else {
                System.out.println("시시");
                throw new RuntimeException("Failed to upload image to Supabase. Response: " + response);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error uploading image to Supabase", e);
        }
    }
}
