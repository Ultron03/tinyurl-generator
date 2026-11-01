package com.tinyurl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class ShortenRequest {

    @NotBlank(message = "URL cannot be blank")
    @URL(message = "Must be a valid URL")
    private String longUrl;

	@Size(min = 3, max = 20, message = "Alias must be 3-20 characters")
	@Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Alias may only contain letters, digits, - and _")
    private String customAlias;

    private Integer expiryDays;
}
