package com.dividend.scheduler;

import com.dividend.model.Company;
import com.dividend.model.ScrapedResult;
import com.dividend.model.constants.CacheKey;
import com.dividend.persist.CompanyRepository;
import com.dividend.persist.DividendRepository;
import com.dividend.persist.entity.CompanyEntity;
import com.dividend.persist.entity.DividendEntity;
import com.dividend.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {
    // 저장돼 있는 회사 정보를 주기적으로 가져오고,
    // 가져온 회사 정보를 스크래핑하고, 업데이트된 정보가 있다면
    // 해당 정보를 추가한다.

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    private final Scraper yahooFinanceScraper;

    // value : Redis 서버의 key 의 prefix, allEntries : Redis Cache 에 있는 Finance 에 해당하는 모든 데이터를 비움
    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}") // 0초 0분 0시 매일 매달 모든요일
    public void yahooFinanceScheduling() {
        log.info("scraping scheduler is started");

        // 저장된 회사 목록을 조회
        List<CompanyEntity> companies = this.companyRepository.findAll();

        // 회사마다 배당금 정보를 새로 스크래핑
        for (CompanyEntity company : companies) {
            log.info("Company scraping -> {}", company.getName());

            ScrapedResult scrapResult = this.yahooFinanceScraper.scrap(
                    new Company(company.getTicker(), company.getName())
            );

            // 스크래핑한 배당금 정보 중, 데이터베이스에 없는 값은 저장
            scrapResult.getDividends().stream()
                    // Dividend 모델을 DividendEntity 로 매핑
                    .map(e -> new DividendEntity(company.getId(), e))
                    // 요소 하나씩 DividendRepository 에 삽입
                    .forEach(e -> {
                        boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exists) {
                            this.dividendRepository.save(e);
                            log.info("insert new dividend -> " + e.toString());
                        }
                    });
            // 스크래핑하려는 사이트에 연속적으로 요청을 보내면 해당 서버에
            // 부하가 갈 수 있기 때문에 요청을 할 땐 텀을 주어야 한다.
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }
}
