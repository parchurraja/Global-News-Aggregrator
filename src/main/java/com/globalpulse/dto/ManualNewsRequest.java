package com.globalpulse.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ManualNewsRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String source;
    @NotBlank
    private String url;
    @NotBlank
    private String category;
}
