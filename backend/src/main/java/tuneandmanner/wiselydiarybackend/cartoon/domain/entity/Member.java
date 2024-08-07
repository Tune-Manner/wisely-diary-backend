package tuneandmanner.wiselydiarybackend.cartoon.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberCode;
    private String memberId;
    private String memberEmail;
    private LocalDateTime joinAt;
    private LocalDateTime withdrawAt;
    private String password;
    private String memberName;
    private String memberStatus;


}
