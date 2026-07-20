package com.project.models.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistrationDTO {
    @NotBlank
    private String fullName;
    @NotBlank
    private String phone;
    @Email
    private String email;
    @Min(18)
    private int age;
    @Size(min = 2, max = 30)
    private String username;
    @Pattern(regexp = "[A-z0-9]{9,}")
    private String password;
}
