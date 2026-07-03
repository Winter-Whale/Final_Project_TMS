package com.project.models.dto.User;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRequestDTO {
    @Size(min = 3, max = 50)
    private String username;
    @Pattern(regexp = "[A-z0-9]{3,}")
    private String password;
}
