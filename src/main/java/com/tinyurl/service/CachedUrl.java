package com.tinyurl.service;

import java.io.Serializable;
import java.time.LocalDateTime;

public record CachedUrl(Long id, String longUrl, String shortCode, LocalDateTime expiresAt) implements Serializable {
	
	public CachedUrl(Long id, String longUrl, LocalDateTime expiresAt) {
		this(id, longUrl, null, expiresAt);
	}
}
