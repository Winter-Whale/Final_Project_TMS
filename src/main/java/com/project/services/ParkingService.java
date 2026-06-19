package com.project.services;

import com.project.models.ParkingSpot;
import com.project.models.User;
import com.project.models.dto.ParkingRequestDTO;
import com.project.models.dto.ParkingResponseDTO;
import com.project.repositories.ParkingRepository;
import com.project.repositories.UserRepository;
import com.project.util.ParkingSpotMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
@Transactional
public class ParkingService {
    private final ParkingRepository parkingRepository;
    private final UserRepository userRepository;
    private final ParkingSpotMapper parkingSpotMapper;

    public ParkingResponseDTO createSpot(ParkingRequestDTO req) {
        User owner = userRepository.findById(req.getOwnerId()).orElseThrow(() -> new RuntimeException("User not found"));
        ParkingSpot spot = parkingSpotMapper.mapFromParkingRequestDTOToParkingSpot(req);
        spot.setUser(owner);
        ParkingSpot saved = parkingRepository.save(spot);
        return parkingSpotMapper.mapFromParkingSpotToParkingResponseDTO(saved);
    }

    public Page<ParkingResponseDTO> getAllSpots(Pageable pageable) {
        return parkingRepository.findAll(pageable).map(parkingSpotMapper::mapFromParkingSpotToParkingResponseDTO);
    }

    public ParkingResponseDTO getSpotById(Integer id) {
        ParkingSpot spot = parkingRepository.findById(id).orElseThrow(() -> new RuntimeException("Spot not found witch id: " + id));
        return parkingSpotMapper.mapFromParkingSpotToParkingResponseDTO(spot);
    }

    public List<ParkingResponseDTO> getSpotByUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found witch id: " + userId);
        }
        return parkingRepository.findByUserId(userId).stream().map(parkingSpotMapper::mapFromParkingSpotToParkingResponseDTO).collect(Collectors.toList());
    }

    public ParkingResponseDTO updateSpot(Integer spotId, ParkingRequestDTO req, Integer currentUserId) {
        ParkingSpot spot = parkingRepository.findById(spotId).orElseThrow(() -> new RuntimeException("Spot not found with id: " + spotId));
        if (spot.getUser() == null || !Objects.equals(spot.getUser().getId(), currentUserId)) {
            throw new RuntimeException("You are not the owner of this spot");
        }
        spot.setAddress(req.getAddress());
        spot.setDescription(req.getDescription());
        spot.setPricePerHour(req.getPricePerHour());
        spot.setStatus(req.getStatus());

        ParkingSpot updated = parkingRepository.save(spot);
        return parkingSpotMapper.mapFromParkingSpotToParkingResponseDTO(updated);
    }

    public void deleteSpot(Integer spotId, Integer currentUserId) {
        ParkingSpot spot = parkingRepository.findById(spotId).orElseThrow(() -> new RuntimeException("Spot not found with id " + spotId));
        if (!Objects.equals(spot.getUser().getId(), currentUserId)) {
            throw new RuntimeException("You are not the owner this spot");
        }
        parkingRepository.delete(spot);
    }
}
