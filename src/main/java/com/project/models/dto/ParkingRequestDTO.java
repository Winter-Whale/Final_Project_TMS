package com.project.models.dto;

import com.project.models.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ParkingRequestDTO {
    @NotBlank
    private String address;
    @NotBlank
    private String description;
    @NotNull
    @Positive
    private int pricePerHour;
    @NotNull
    private Status status;
    @NotNull
    private int ownerId;
}
