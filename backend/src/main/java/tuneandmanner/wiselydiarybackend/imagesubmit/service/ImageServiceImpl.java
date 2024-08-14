package tuneandmanner.wiselydiarybackend.imagesubmit.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import tuneandmanner.wiselydiarybackend.common.exception.ResourceNotFoundException;
import tuneandmanner.wiselydiarybackend.common.exception.S3OperationException;
import tuneandmanner.wiselydiarybackend.imagesubmit.domain.entity.Image;
import tuneandmanner.wiselydiarybackend.imagesubmit.domain.repository.ImageRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageServiceImpl.class);
    private final ImageRepository imageRepository;
    private final S3Client s3Client;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.bucket}")
    private String bucketName;

    @Override
    @Transactional
    public List<Image> uploadImages(Long diaryCode, List<MultipartFile> images) {
        List<Image> uploadedImages = new ArrayList<>();

        for (MultipartFile file : images) {
            try {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .contentType(file.getContentType())
                        .build();

                s3Client.putObject(putObjectRequest,
                        RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

                String imageUrl = String.format("%s/storage/v1/object/public/%s/%s",
                        supabaseUrl, bucketName, fileName);

                Image image = Image.builder()
                        .diaryCode(diaryCode)
                        .imagePath(imageUrl)
                        .imageStatus("ACTIVE")
                        .build();

                uploadedImages.add(imageRepository.save(image));
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image: " + file.getOriginalFilename(), e);
            }
        }

        return uploadedImages;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Image> getImagesByDiaryCode(Long diaryCode) {
        return imageRepository.findByDiaryCode(diaryCode);
    }

    @Override
    @Transactional
    public void deleteImage(Long imageCode) {
        Image image = imageRepository.findById(imageCode)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with code: " + imageCode));

        String key = extractKeyFromImagePath(image.getImagePath());

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted S3 object. Bucket: {}, Key: {}", bucketName, key);
        } catch (NoSuchKeyException e) {
            log.warn("Object not found in S3 bucket. Bucket: {}, Key: {}. Proceeding with database deletion.", bucketName, key);
        } catch (S3Exception e) {
            log.error("Error deleting S3 object. Bucket: {}, Key: {}", bucketName, key, e);
            throw new S3OperationException("Failed to delete image from S3", e);
        }

        imageRepository.delete(image);
        log.info("Successfully deleted image from database. Image code: {}", imageCode);
    }

    private String extractKeyFromImagePath(String imagePath) {
        // URL에서 키(파일 이름)를 추출하는 로직
        // 예: https://your-bucket.s3.region.amazonaws.com/folder/image.jpg에서 "folder/image.jpg"를 추출
        String[] parts = imagePath.split("/");
        return parts[parts.length - 1];
    }
}