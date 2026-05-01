package com.tinyurl.controller;

import com.tinyurl.dto.AnalyticsResponse;
import com.tinyurl.dto.ShortenRequest;
import com.tinyurl.dto.ShortenResponse;
import com.tinyurl.exception.GlobalExceptionHandler;
import com.tinyurl.service.UrlService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/api/v1/shorten")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(urlService.shorten(request));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode,HttpServletRequest request) {
      String ip = request.getHeader("X-Forwarded-For");                                                                                                                                                                                                     
      if (ip == null) ip = request.getRemoteAddr();  // fallback if no proxy                                                                                                                                                                                
                                                                                                                                                                                                                                                            
      String userAgent = request.getHeader("User-Agent");                                                                                                                                                                                                   
                                                                                                                                                                                                                                                            
      String longUrl = urlService.getLongUrl(shortCode, ip, userAgent);                                                                                                                                                                                     
      HttpHeaders headers = new HttpHeaders();
      headers.setLocation(URI.create(longUrl));                                                                                                                                                                                                             
      return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    @GetMapping("api/v1/analytics/{shortCode}")
    public ResponseEntity<AnalyticsResponse> analyticsOfUrl(@PathVariable String shortCode) {
        return ResponseEntity.ok(urlService.getAnalytics(shortCode));
    }
}
