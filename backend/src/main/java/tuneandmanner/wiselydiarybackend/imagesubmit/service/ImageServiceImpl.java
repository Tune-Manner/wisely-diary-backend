package tuneandmanner.wiselydiarybackend.imagesubmit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import tuneandmanner.wiselydiarybackend.imagesubmit.domain.entity.Image;
import tuneandmanner.wiselydiarybackend.imagesubmit.domain.repository.ImageRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

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
                .orElseThrow(() -> new RuntimeException("Image not found"));

        // Delete from S3
        String key = image.getImagePath().substring(image.getImagePath().lastIndexOf('/') + 1);
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);

        // Delete from database
        imageRepository.delete(image);
    }
}