package com.tinyurl.service;

import com.tinyurl.dto.ShortenRequest;
import com.tinyurl.dto.ShortenResponse;
import com.tinyurl.exception.UrlExpiredException;
import com.tinyurl.exception.UrlNotFoundException;
import com.tinyurl.model.Url;
import com.tinyurl.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final Base62Service base62Service;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public ShortenResponse shorten(ShortenRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Url url = Url.builder()
                .longUrl(request.getLongUrl())
                .createdAt(now)
                .expiresAt(request.getExpiryDays() != null ? now.plusDays(request.getExpiryDays()) : null)
                .build();

        url = urlRepository.save(url);

        String shortCode = (request.getCustomAlias() != null && !request.getCustomAlias().isBlank())
                ? request.getCustomAlias()
                : base62Service.encode(url.getId());

        url.setShortCode(shortCode);
        urlRepository.save(url);

        return ShortenResponse.builder()
                .shortUrl(baseUrl + "/" + shortCode)
                .shortCode(shortCode)
                .longUrl(url.getLongUrl())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .build();
    }

    @Transactional
    public String getLongUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException(shortCode);
        }

        url.setClickCount(url.getClickCount() + 1);

        return url.getLongUrl();
    }
}
