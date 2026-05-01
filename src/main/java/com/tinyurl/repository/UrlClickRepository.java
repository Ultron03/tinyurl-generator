package com.tinyurl.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.tinyurl.model.UrlClick;

public interface UrlClickRepository extends JpaRepository<UrlClick, Long> {
      long countByUrlIdAndClickedAtAfter(Long urlId, LocalDateTime since);

      @Modifying                                                                                                                                                                                                                                                
      @Query("DELETE FROM UrlClick uc WHERE uc.url.id IN " +
            "(SELECT u.id FROM Url u WHERE u.expiresAt IS NOT NULL AND u.expiresAt < :now)")                                                                                                                                                                   
      int deleteClicksForExpiredUrls(LocalDateTime now);
                                                                                                                                                                                                                                                                  
      @Modifying      
      @Query("DELETE FROM UrlClick uc WHERE uc.url.id IN " +                                                                                                                                                                                                    
            "(SELECT u.id FROM Url u WHERE u.lastAccessTime <= :cutoff AND u.clickCount < 10)")
      int deleteClicksForStaleUrls(LocalDateTime cutoff); 

}
