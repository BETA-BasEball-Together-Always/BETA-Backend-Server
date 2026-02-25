package com.beta.core.infra.client.kbo;

import com.beta.core.config.CacheConfig;
import com.beta.core.exception.kbo.KboApiException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class KboRankingClient {

    private static final String KBO_RANKING_URL = "https://www.koreabaseball.com/Record/TeamRank/TeamRankDaily.aspx";
    private static final int TIMEOUT_MS = 10000;

    @Cacheable(value = CacheConfig.KBO_RANKING_CACHE, key = "'rankings'")
    public List<TeamRanking> getRankings() {
        return crawlRankings();
    }

    @CacheEvict(value = CacheConfig.KBO_RANKING_CACHE, key = "'rankings'")
    public void evictCache() {
        log.info("KBO 순위 캐시 제거됨");
    }

    public void refreshCache() {
        evictCache();
        try {
            List<TeamRanking> rankings = getRankings();
            log.info("KBO 순위 캐시 갱신 완료: {} 팀", rankings.size());
        } catch (KboApiException e) {
            log.warn("KBO 순위 캐시 갱신 실패: {}", e.getMessage());
        }
    }

    private List<TeamRanking> crawlRankings() {
        try {
            Document doc = Jsoup.connect(KBO_RANKING_URL)
                    .timeout(TIMEOUT_MS)
                    .get();

            Element table = doc.selectFirst("table.tData");
            if (table == null) {
                log.warn("KBO 순위 테이블을 찾을 수 없음");
                throw new KboApiException();
            }

            Elements rows = table.select("tbody tr");
            if (rows.isEmpty()) {
                log.warn("KBO 순위 데이터 행이 없음");
                throw new KboApiException();
            }

            List<TeamRanking> rankings = new ArrayList<>();
            for (Element row : rows) {
                Elements cells = row.select("td");
                if (cells.size() < 10) {
                    continue;
                }

                try {
                    TeamRanking ranking = new TeamRanking(
                            parseIntSafe(cells.get(0).text()),
                            cells.get(1).text().trim(),
                            parseIntSafe(cells.get(2).text()),
                            parseIntSafe(cells.get(3).text()),
                            parseIntSafe(cells.get(4).text()),
                            parseIntSafe(cells.get(5).text()),
                            cells.get(6).text().trim(),
                            cells.get(7).text().trim(),
                            cells.get(8).text().trim(),
                            cells.get(9).text().trim()
                    );
                    rankings.add(ranking);
                } catch (Exception e) {
                    log.debug("행 파싱 실패: {}", e.getMessage());
                }
            }

            if (rankings.isEmpty()) {
                log.warn("파싱된 KBO 순위 데이터 없음");
                throw new KboApiException();
            }

            return rankings;
        } catch (KboApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("KBO 순위 크롤링 실패: {}", e.getMessage());
            throw new KboApiException(e);
        }
    }

    private int parseIntSafe(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(text.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
