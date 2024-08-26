package tuneandmanner.wiselydiarybackend.imagesubmit.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tuneandmanner.wiselydiarybackend.imagesubmit.domain.entity.Image;
import tuneandmanner.wiselydiarybackend.imagesubmit.service.ImageService;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<List<Image>> uploadImages(
            @RequestParam("diaryCode") Long diaryCode,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        List<Image> uploadedImages = imageService.uploadImages(diaryCode, images);
        return ResponseEntity.ok(uploadedImages);
    }

    @GetMapping("/diary/{diaryCode}")
    public ResponseEntity<List<Image>> getImagesByDiaryCode(@PathVariable Long diaryCode) {
        List<Image> images = imageService.getImagesByDiaryCode(diaryCode);
        return ResponseEntity.ok(images);
    }

    @DeleteMapping("/{imageCode}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageCode) {
        imageService.deleteImage(imageCode);
        return ResponseEntity.noContent().build();
    }
}