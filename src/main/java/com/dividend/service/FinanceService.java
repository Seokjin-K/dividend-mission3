package com.dividend.service;

import com.dividend.exception.impl.NoCompanyException;
import com.dividend.model.Company;
import com.dividend.model.Dividend;
import com.dividend.model.ScrapedResult;
import com.dividend.model.constants.CacheKey;
import com.dividend.persist.CompanyRepository;
import com.dividend.persist.DividendRepository;
import com.dividend.persist.entity.CompanyEntity;
import com.dividend.persist.entity.DividendEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    // @Cacheable 붙은 메서드는 캐시에 데이터가 없을 경우에는 메서드를 실행하고,
    // 반환 값을 캐시에 추가, 캐시에 데이터가 있을 경우 메서드를 실행하지 않고,
    // 캐시에 있는 데이터를 반환
    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE) // 해당 key-value 는 Redis 의 key-value 와는 의미가 다름.
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search company -> " + companyName);

        // 1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity company = this.companyRepository.findByName(companyName)
                .orElseThrow(NoCompanyException::new);
        // 값이 없으면 인자로 넘겨주는 예외를 발생, 값이 있다면 Optional 을 언박싱한 데이터를 반환.

        // 2. 조회된 회사 ID로 배당금을 조회
        List<DividendEntity> dividendEntities = this.dividendRepository.findAllByCompanyId(company.getId());

        // 3. 결과 조합 후, 반환
        List<Dividend> dividends = new ArrayList<>();

        for (var entity : dividendEntities) {
            dividends.add(new Dividend(entity.getDate(), entity.getDividend()));
        }
        return new ScrapedResult(new Company(company.getTicker(), company.getTicker()), dividends);
    }
}
