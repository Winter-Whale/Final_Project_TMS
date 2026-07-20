package com.project.models.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateDTO {
    @NotBlank
    private String fullName;
    @NotBlank
    private String phone;
    @NotBlank
    @Email
    private String email;
    @Min(18)
    private int age;
}
