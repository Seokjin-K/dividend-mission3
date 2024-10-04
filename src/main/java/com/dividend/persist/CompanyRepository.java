package com.dividend.persist;

import com.dividend.persist.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {

    boolean existsByTicker(String ticker);

    // Optional<> : 값이 존재할 수도 있고 존재하지 않을 수도 있는 객체를 감싸는 컨테이너.
    // 데이터베이스에서 엔티티를 조회할 때 해당 엔티티가 없을 경우 null 을
    // 반환하지 않는 대신, Optional 객체로 감싸서 반환하기 위함.
    // 데이터를 사용하는 쪽에서 바로 null 처리를 하지 않아도 되고, isPresent(),
    // orElse(), orElseThrow() 등과 같은 메서드를 통해 값을 안전하게 다룰 수 있다.
    Optional<CompanyEntity> findByName(String name);
}
