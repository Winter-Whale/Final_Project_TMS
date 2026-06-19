package com.project.models.dto;

import com.project.models.Status;
import lombok.Data;

@Data
public class ParkingResponseDTO {
    private Integer id;
    private String address;
    private String description;
    private Integer pricePerHour;
    private Status status;
    private Integer ownerId;
    private String ownerFullName;
}
