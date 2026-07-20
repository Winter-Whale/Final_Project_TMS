package com.project.util;

import com.project.models.ParkingSpot;
import com.project.models.User;
import com.project.models.dto.parking.ParkingRequestDTO;
import com.project.models.dto.parking.ParkingResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ParkingSpotMapper {
    public ParkingSpot mapFromParkingRequestDTOToParkingSpot(ParkingRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new NullPointerException();
        }
        ParkingSpot parkingSpot = new ParkingSpot();
        parkingSpot.setAddress(requestDTO.getAddress());
        parkingSpot.setDescription(requestDTO.getDescription());
        parkingSpot.setPricePerHour(requestDTO.getPricePerHour());
        parkingSpot.setStatus(requestDTO.getStatus());
        return parkingSpot;
    }

    public ParkingResponseDTO mapFromParkingSpotToParkingResponseDTO(ParkingSpot spot) {
        if (spot == null) {
            throw new NullPointerException();
        }
        ParkingResponseDTO response = new ParkingResponseDTO();
        response.setId(spot.getId());
        response.setAddress(spot.getAddress());
        response.setDescription(spot.getDescription());
        response.setPricePerHour(spot.getPricePerHour());
        response.setStatus(spot.getStatus());

        User user = spot.getUser();
        response.setOwnerId(user.getId());
        response.setOwnerFullName(user.getFullName());
        return response;
    }
}
