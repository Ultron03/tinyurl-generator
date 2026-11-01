package com.tinyurl.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.tinyurl.exception.UrlNotFoundException;
import com.tinyurl.model.Url;
import com.tinyurl.repository.UrlRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlLookUpService {
	private final UrlRepository urlRepository;

	@Cacheable(cacheNames = "urls", key = "#shortCode")
	public CachedUrl find(String shortCode) {
		Url url = urlRepository.findByShortCode(shortCode)
				.orElseThrow(() -> new UrlNotFoundException(shortCode));

		return new CachedUrl(url.getId(), url.getLongUrl(), url.getExpiresAt());
	}
}
