package com.dividend.persist.entity;

import com.dividend.model.Dividend;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "DIVIDEND")
@Getter
@ToString
@NoArgsConstructor
@Table(
        uniqueConstraints = {
                // 복합 유니크 키 설정. 두 값이 모두 동일할 때 중복.
                // 중복된 데이터를 저장하려고 할 때 예외 발생
                // INSERT IGNORE 또는 ON DUPLICATE KEY UPDATE 을 사용하여 처리
                @UniqueConstraint(columnNames = {"companyId", "date"})
        }
)
public class DividendEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId; // 어떤 회사의 배당금인지

    private LocalDateTime date;

    private String dividend;

    public DividendEntity(Long companyId, Dividend dividend) {
        this.companyId = companyId;
        this.date = dividend.getDate();
        this.dividend = dividend.getDividend();
    }
}
