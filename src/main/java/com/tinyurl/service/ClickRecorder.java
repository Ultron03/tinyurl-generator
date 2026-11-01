package com.tinyurl.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.tinyurl.model.UrlClick;
import com.tinyurl.repository.UrlClickRepository;
import com.tinyurl.repository.UrlRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickRecorder {
	  private final UrlRepository urlRepository;
	  private final UrlClickRepository urlClickRespository;

	  @Async
	  @Transactional
	  public void recordClick(Long urlId, String ipAddress, String userAgent, LocalDateTime clickedAt) {
		urlRepository.recordAccess(urlId, clickedAt);
		urlClickRespository.save(UrlClick.builder()
				.url(urlRepository.getReferenceById(urlId))
				.clickedAt(clickedAt)
				.ipAddress(ipAddress)
				.userAgent(userAgent)
				.build());
		log.debug("Click recorded — urlId={}, ip={}, userAgent={}", urlId, ipAddress, userAgent);
	  }
}
