package tuneandmanner.wiselydiarybackend.member.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String memberId;
    private String memberEmail;
    private LocalDateTime joinAt;
    private LocalDateTime withdrawAt;
//    private String password;
    private String memberName;
    private String memberStatus;
    private String memberGender;
    private LocalDate memberAge;


}
