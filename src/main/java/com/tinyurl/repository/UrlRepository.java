package com.tinyurl.repository;

import com.tinyurl.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);

    @Modifying
    @Query("DELETE FROM Url u WHERE u.expiresAt < :now")
    int deleteWhereExpiresAtBefore(LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Url u WHERE u.lastAccessTime <= :now AND u.clickCount < 10")
    int deleteWhereLastAccessTimeBefore90DaysAndClickCountIsLessThan10(LocalDateTime now);
}
