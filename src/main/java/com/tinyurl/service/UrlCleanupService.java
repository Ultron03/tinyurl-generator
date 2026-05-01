package com.tinyurl.service;

import com.tinyurl.repository.UrlClickRepository;
import com.tinyurl.repository.UrlRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlCleanupService{

      private final UrlRepository urlRepository;
      private final UrlClickRepository urlClickRepository;

      @Scheduled(cron = "0 0 2 * * *")
      @Transactional
      public void cleanupUrls(){
            log.info("cleanupUrls() start — running scheduled URL cleanup");

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime cutoff = now.minusMonths(3);

            log.debug("cleanupUrls() tier-1 — deleting clicks for expired URLs (expiresAt < {})", now);
            urlClickRepository.deleteClicksForExpiredUrls(now);
            int expiredCount = urlRepository.deleteWhereExpiresAtBefore(now);
            log.info("cleanupUrls() tier-1 — deleted {} expired URLs and their clicks", expiredCount);

            log.debug("cleanupUrls() tier-2 — deleting clicks for stale URLs (lastAccessTime < {}, clickCount < 10)", cutoff);
            urlClickRepository.deleteClicksForStaleUrls(cutoff);
            int staleCount = urlRepository.deleteWhereLastAccessTimeBefore90DaysAndClickCountIsLessThan10(cutoff);
            log.info("cleanupUrls() tier-2 — deleted {} stale URLs and their clicks", staleCount);

            log.info("cleanupUrls() end — total deleted: {} expired + {} stale = {} URLs",
                    expiredCount, staleCount, expiredCount + staleCount);
      }
}