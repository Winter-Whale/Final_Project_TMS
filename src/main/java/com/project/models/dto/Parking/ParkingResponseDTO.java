package com.project.models.dto.Parking;

import com.project.models.Status;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ParkingResponseDTO {
    private Integer id;
    private String address;
    private String description;
    private BigDecimal pricePerHour;
    private Status status;
    private Integer ownerId;
    private String ownerFullName;
}
