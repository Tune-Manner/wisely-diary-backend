package tuneandmanner.wiselydiarybackend.cartoon.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tuneandmanner.wiselydiarybackend.cartoon.domain.entity.Cartoon;

public interface CartoonRepository extends JpaRepository<Cartoon, Integer> {
    // 필요한 쿼리 메서드 추가
}