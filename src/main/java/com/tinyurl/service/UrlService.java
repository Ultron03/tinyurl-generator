package com.tinyurl.service;

import com.tinyurl.dto.AnalyticsResponse;
import com.tinyurl.dto.ShortenRequest;
import com.tinyurl.dto.ShortenResponse;
import com.tinyurl.exception.UrlExpiredException;
import com.tinyurl.exception.UrlNotFoundException;
import com.tinyurl.model.Url;
import com.tinyurl.model.UrlClick;
import com.tinyurl.repository.UrlClickRepository;
import com.tinyurl.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final UrlClickRepository urlClickRepository;
    private final Base62Service base62Service;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public ShortenResponse shorten(ShortenRequest request) {
        log.info("shorten() start — longUrl={}, customAlias={}, expiryDays={}",
                request.getLongUrl(), request.getCustomAlias(), request.getExpiryDays());

        LocalDateTime now = LocalDateTime.now();

        Url url = Url.builder()
                .longUrl(request.getLongUrl())
                .createdAt(now)
                .expiresAt(request.getExpiryDays() != null ? now.plusDays(request.getExpiryDays()) : null)
                .lastAccessTime(now)
                .build();

        url = urlRepository.save(url);
        log.debug("URL entity persisted — id={}", url.getId());

        String shortCode = (request.getCustomAlias() != null && !request.getCustomAlias().isBlank())
                ? request.getCustomAlias()
                : base62Service.encode(url.getId());

        url.setShortCode(shortCode);
        urlRepository.save(url);

        log.info("URL created — id={}, shortCode={}, expiresAt={}", url.getId(), shortCode, url.getExpiresAt());

        return ShortenResponse.builder()
                .shortUrl(baseUrl + "/" + shortCode)
                .shortCode(shortCode)
                .longUrl(url.getLongUrl())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .build();
    }

    @Transactional
    public String getLongUrl(String shortCode, String ipAddress, String userAgent) {
        log.info("getLongUrl() start — shortCode={}, ip={}", shortCode, ipAddress);

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.warn("URL not found — shortCode={}", shortCode);
                    return new UrlNotFoundException(shortCode);
                });

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("URL expired — shortCode={}, expiredAt={}", shortCode, url.getExpiresAt());
            throw new UrlExpiredException(shortCode);
        }

        url.setLastAccessTime(LocalDateTime.now());
        url.setClickCount(url.getClickCount() + 1);

        UrlClick urlClick = UrlClick.builder()
                .url(url)
                .clickedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        urlClickRepository.save(urlClick);
        log.info("Click recorded — shortCode={}, urlId={}, totalClicks={}, ip={}",
                shortCode, url.getId(), url.getClickCount(), ipAddress);

        log.info("getLongUrl() end — shortCode={} redirecting to {}", shortCode, url.getLongUrl());
        return url.getLongUrl();
    }

    public AnalyticsResponse getAnalytics(String shortCode) {
        log.info("getAnalytics() start — shortCode={}", shortCode);

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.warn("Analytics requested for unknown shortCode={}", shortCode);
                    return new UrlNotFoundException(shortCode);
                });

        long clicksLast24Hours = urlClickRepository.countByUrlIdAndClickedAtAfter(url.getId(), LocalDateTime.now().minusDays(1));
        long clicksLast7Days = urlClickRepository.countByUrlIdAndClickedAtAfter(url.getId(), LocalDateTime.now().minusDays(7));

        log.info("getAnalytics() end — shortCode={}, totalClicks={}, last24h={}, last7d={}",
                shortCode, url.getClickCount(), clicksLast24Hours, clicksLast7Days);

        return AnalyticsResponse.builder()
                .shortCode(url.getShortCode())
                .longUrl(url.getLongUrl())
                .totalClicks(url.getClickCount())
                .clicksLast24Hours(clicksLast24Hours)
                .clicksLast7Days(clicksLast7Days)
                .lastAccessedAt(url.getLastAccessTime())
                .build();
    }
}
