package com.globalpulse.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
