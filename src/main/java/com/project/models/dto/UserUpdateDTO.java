package com.project.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @Positive
    private int id;
    @NotBlank
    private String fullName;
    @NotBlank
    private String phone;
    @NotBlank
    @Email
    private String email;
    @Min(18)
    private int age;
    @Size(min = 2, max = 30)
    private String username;
    @Pattern(regexp = "[A-z]{9,}")
    private String password;
}
