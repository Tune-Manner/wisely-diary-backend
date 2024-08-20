package tuneandmanner.wiselydiarybackend.imagesubmit.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_code")
    private Long imageCode;

    @Column(name = "diary_code")
    private Long diaryCode;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "image_status", nullable = false)
    private String imageStatus;

    @Builder
    public Image(Long diaryCode, String imagePath, String imageStatus) {
        this.diaryCode = diaryCode;
        this.imagePath = imagePath;
        this.imageStatus = imageStatus;
    }
}