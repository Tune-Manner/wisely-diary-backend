package tuneandmanner.wiselydiarybackend.cartoon.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuneandmanner.wiselydiarybackend.cartoon.domain.entity.Cartoon;
import tuneandmanner.wiselydiarybackend.cartoon.dto.response.InquiryCartoonResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface CartoonRepository extends JpaRepository<Cartoon, Integer> {
    // 필요한 쿼리 메서드 추가

    /*날짜에 해당하는 만화 조회*/
    List<Cartoon> findByCreatedAtBetween(LocalDateTime startOfDay,LocalDateTime endOfDay);
}