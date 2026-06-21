package com.project.models.dto.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SecurityUpdateDTO {
    @NotBlank
    private String currentPassword;
    @NotBlank
    @Size(min = 2, max = 30)
    private String newUserName;
    @NotBlank
    @Pattern(regexp = "[A-z0-9]{9,}")
    private String newPassword;
}
