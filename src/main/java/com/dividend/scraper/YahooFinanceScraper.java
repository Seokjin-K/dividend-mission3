package com.dividend.scraper;

import com.dividend.exception.impl.CompanyNotFoundException;
import com.dividend.model.Company;
import com.dividend.model.Dividend;
import com.dividend.model.ScrapedResult;
import com.dividend.model.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper {

    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history/?frequency=1mo&period1=%d&period2=%d&";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";

    private static final long START_TIME = 86400; // 60 * 60 * 24

    @Override
    public ScrapedResult scrap(Company company) {

        var scarpResult = new ScrapedResult();
        scarpResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000;// 1970년1월1부터 현재까지 경과한 시간을 ms로 가져온 시간을 초 단위로 변환

            String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
            Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                    .timeout(5000)
                    .followRedirects(true);
            Document document = connection.get();
            Elements parsingDivs = document.select("table tbody tr");
            List<Dividend> dividends = new ArrayList<>();

            for (Element e : parsingDivs) {
                Elements td = e.select("td");

                if (td.size() > 1 && td.get(1).text().contains("Dividend")) {
                    String dividendValue = td.get(1).select("span").text();
                    String dateText = td.get(0).text();

                    String[] dateSplits = dateText.split(" ");
                    int month = Month.strToNumber(dateSplits[0]);
                    int day = Integer.parseInt(dateSplits[1].replace(",", ""));
                    int year = Integer.parseInt(dateSplits[2]);

                    if (month < 0) {
                        throw new RuntimeException("Unexpected Month enum value -> " + dateSplits[0]);
                    }

                    dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividendValue));
                }
            }
            scarpResult.setDividends(dividends);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return scarpResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker, ticker);

        try {
            Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                    .timeout(5000)
                    .followRedirects(true);

            Document document = connection.get();
            Elements h1Elements = document.getElementsByTag("h1");
            if (h1Elements.size() < 2) {
                throw new CompanyNotFoundException();
            }

            Element titleElem = h1Elements.get(1);
            String title = titleElem.text().split("\\(")[0].trim();

            return new Company(ticker, title);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
