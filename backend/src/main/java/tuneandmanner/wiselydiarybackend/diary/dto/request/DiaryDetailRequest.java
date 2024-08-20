package tuneandmanner.wiselydiarybackend.diary.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryDetailRequest {
    private String memberId;
    private String date;
}
