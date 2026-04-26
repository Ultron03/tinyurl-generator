package com.tinyurl.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class ShortenRequest {

    @NotBlank(message = "URL cannot be blank")
    @URL(message = "Must be a valid URL")
    private String longUrl;

    private String customAlias;

    private Integer expiryDays;
}
