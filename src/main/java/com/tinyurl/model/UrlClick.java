package com.tinyurl.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "url_click")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlClick {
      
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;

      @ManyToOne
      @JoinColumn(name = "url_id", nullable = false,foreignKey = @ForeignKey(name = "fk_url_click_url_id"))
      private Url url;

      @Column(nullable = false)
      private LocalDateTime clickedAt;
      
      @Column(length = 255)
      private String ipAddress;

      @Column(length = 255)
      private String userAgent;
}
