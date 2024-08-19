package tuneandmanner.wiselydiarybackend.emotion.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name="emotion")
@Entity
public class Emotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emotion_code")
    private Integer emotionCode;

    @Column(name = "emotion_type")
    private String emotionType;
}
