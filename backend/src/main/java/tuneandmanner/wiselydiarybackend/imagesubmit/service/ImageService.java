package tuneandmanner.wiselydiarybackend.imagesubmit.service;

import org.springframework.web.multipart.MultipartFile;
import tuneandmanner.wiselydiarybackend.imagesubmit.domain.entity.Image;

import java.util.List;

public interface ImageService {
    List<Image> uploadImages(Long diaryCode, List<MultipartFile> images);
    List<Image> getImagesByDiaryCode(Long diaryCode);
    void deleteImage(Long imageCode);
}
