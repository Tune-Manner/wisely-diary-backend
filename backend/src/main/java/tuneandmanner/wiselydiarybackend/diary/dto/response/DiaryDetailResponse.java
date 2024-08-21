package tuneandmanner.wiselydiarybackend.diary.dto.response;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DiaryDetailResponse {
    private Long diaryCode;
    private String date;
    private String diaryContents;
}
