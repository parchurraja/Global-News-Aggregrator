package com.globalpulse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotUsernameRequest {
    @NotBlank
    @Email
    private String email;
}
