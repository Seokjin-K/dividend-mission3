package com.dividend.persist;

import com.dividend.persist.entity.DividendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity, Long> {

    List<DividendEntity> findAllByCompanyId(Long id);

    @Transactional
    void deleteAllByCompanyId(Long id);

    // companyId 와 date 에 복합 유니크 키 설정이 돼 있기 때문에 검색이 빠르다.
    boolean existsByCompanyIdAndDate(Long companyId, LocalDateTime date);
}
