package com.tinyurl.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalyticsResponse {
      private String shortCode;
      private String longUrl;
      private Long totalClicks;
      private Long clicksLast24Hours;
      private Long clicksLast7Days;
      private LocalDateTime lastAccessedAt;
}
