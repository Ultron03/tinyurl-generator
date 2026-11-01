package com.tinyurl.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.tinyurl.dto.AnalyticsResponse;
import com.tinyurl.dto.ShortenRequest;
import com.tinyurl.dto.ShortenResponse;
import com.tinyurl.exception.UrlExpiredException;
import com.tinyurl.exception.UrlNotFoundException;
import com.tinyurl.model.Url;
import com.tinyurl.repository.UrlClickRepository;
import com.tinyurl.repository.UrlRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final UrlClickRepository urlClickRepository;
    private final Base62Service base62Service;
	private static final int MAX_CODE_ATTEMPTS = 5;
	private final ClickRecorder clickRecorder;
	private final UrlLookUpService urlLookUpService;

    @Value("${app.base-url}")
    private String baseUrl;

	public ShortenResponse shorten(ShortenRequest request) {
		log.info("shorten() start — longUrl={}, customAlias={}, expiryDays={}",
				request.getLongUrl(), request.getCustomAlias(), request.getExpiryDays());

		String alias = request.getCustomAlias();
		if (alias != null && !alias.isBlank()) {
			return toResponse(saveWithCode(request, alias));
		}

		for (int attempt = 1; attempt <= MAX_CODE_ATTEMPTS; attempt++) {
			String code = base62Service.randomCode();
			if (urlRepository.existsByShortCode(code)) {
				log.debug("Generated code already taken — code={}, attempt={}", code, attempt);
				continue;
			}
			try {
				return toResponse(saveWithCode(request, code));
			} catch (DataIntegrityViolationException e) {
				log.warn("Generated code collided on insert — code={}, attempt={}", code, attempt);
			}
		}
		throw new IllegalStateException("Could not generate a unique short code after " + MAX_CODE_ATTEMPTS + " attempts");
	}

	private Url saveWithCode(ShortenRequest request, String shortCode) {
		LocalDateTime now = LocalDateTime.now();
		Url url = Url.builder()
				.longUrl(request.getLongUrl())
				.shortCode(shortCode)
				.createdAt(now)
				.expiresAt(request.getExpiryDays() != null ? now.plusDays(request.getExpiryDays()) : null)
				.lastAccessTime(now)
				.build();
		url = urlRepository.save(url);
		log.info("URL created — id={}, shortCode={}, expiresAt={}", url.getId(), shortCode, url.getExpiresAt());
		return url;
	}

	private ShortenResponse toResponse(Url url) {
		return ShortenResponse.builder()
				.shortUrl(baseUrl + "/" + url.getShortCode())
				.shortCode(url.getShortCode())
				.longUrl(url.getLongUrl())
				.createdAt(url.getCreatedAt())
				.expiresAt(url.getExpiresAt())
				.build();
	}

	public String getLongUrl(String shortCode, String ipAddress, String userAgent) {
		log.info("getLongUrl() start — shortCode={}, ip={}", shortCode, ipAddress);

		CachedUrl url = urlLookUpService.find(shortCode);

		if (url.expiresAt() != null && url.expiresAt().isBefore(LocalDateTime.now())) {
			log.warn("URL expired — shortCode={}, expiredAt={}", shortCode, url.expiresAt());
			throw new UrlExpiredException(shortCode);
		}

		clickRecorder.recordClick(url.id(), ipAddress, userAgent, LocalDateTime.now());
		return url.longUrl();
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
