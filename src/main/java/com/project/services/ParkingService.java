package com.project.services;

import com.project.exceptions.RegistrationException;
import com.project.exceptions.SpotNotFoundException;
import com.project.exceptions.UserNotFoundException;
import com.project.models.ParkingSpot;
import com.project.models.User;
import com.project.models.dto.Parking.ParkingRequestDTO;
import com.project.models.dto.Parking.ParkingResponseDTO;
import com.project.repositories.ParkingRepository;
import com.project.repositories.UserRepository;
import com.project.util.ParkingSpotMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
@Transactional
public class ParkingService {
    private final ParkingRepository parkingRepository;
    private final UserRepository userRepository;
    private final ParkingSpotMapper parkingSpotMapper;

    public ParkingResponseDTO createSpot(ParkingRequestDTO req) {
        User owner = userRepository.findById(req.getOwnerId())
                .orElseThrow(() ->  new RegistrationException("Registration error, please check the data is correct"));
        ParkingSpot spot = parkingSpotMapper.mapFromParkingRequestDTOToParkingSpot(req);
        spot.setUser(owner);
        ParkingSpot saved = parkingRepository.save(spot);
        return parkingSpotMapper.mapFromParkingSpotToParkingResponseDTO(saved);
    }

    public List<ParkingResponseDTO> getAllSpots() {
        return parkingRepository.findAll()
                .stream().map(parkingSpotMapper::mapFromParkingSpotToParkingResponseDTO)
                .collect(Collectors.toList());
    }

    public ParkingResponseDTO getSpotById(Integer id) {
        ParkingSpot spot = parkingRepository.findById(id).orElseThrow(() -> new SpotNotFoundException("Spot not found witch id: " + id));
        return parkingSpotMapper.mapFromParkingSpotToParkingResponseDTO(spot);
    }

    public List<ParkingResponseDTO> getSpotByUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            new UserNotFoundException("User not found witch id: " + userId);
        }
        return parkingRepository.findByUserId(userId).stream()
                .map(parkingSpotMapper::mapFromParkingSpotToParkingResponseDTO).collect(Collectors.toList());
    }

    public ParkingResponseDTO updateSpot(Integer spotId, ParkingRequestDTO req) {
        ParkingSpot spot = parkingRepository.findById(spotId)
                .orElseThrow(() -> new SpotNotFoundException("Spot not found with id: " + spotId));
        spot.setAddress(req.getAddress());
        spot.setDescription(req.getDescription());
        spot.setPricePerHour(req.getPricePerHour());
        spot.setStatus(req.getStatus());

        ParkingSpot updated = parkingRepository.save(spot);
        return parkingSpotMapper.mapFromParkingSpotToParkingResponseDTO(updated);
    }

    public void deleteSpot(Integer spotId) {
        ParkingSpot spot = parkingRepository.findById(spotId)
                .orElseThrow(() -> new SpotNotFoundException("Spot not found with id " + spotId));
        parkingRepository.delete(spot);
    }
}
