package com.project.models.dto.Parking;

import com.project.models.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ParkingRequestDTO {
    @NotBlank
    private String address;
    @NotBlank
    private String description;
    @NotNull
    @Positive
    private BigDecimal pricePerHour;
    @NotNull
    private Status status;
    @NotNull
    private int ownerId;
}
