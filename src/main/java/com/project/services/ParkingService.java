package com.project.services;

import com.project.exceptions.RegistrationException;
import com.project.exceptions.SpotNotFoundException;
import com.project.exceptions.UserNotFoundException;
import com.project.exceptions.YouAreNotOwnerException;
import com.project.models.ParkingSpot;
import com.project.models.Role;
import com.project.models.Status;
import com.project.models.User;
import com.project.models.dto.Parking.ParkingRequestDTO;
import com.project.models.dto.Parking.ParkingResponseDTO;
import com.project.repositories.ParkingRepository;
import com.project.repositories.UserRepository;
import com.project.util.ParkingSpotMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ParkingService {

    private final ParkingRepository parkingRepository;
    private final UserRepository userRepository;
    private final ParkingSpotMapper parkingSpotMapper;
    private final CurrentUserService currentUserService;


    public ParkingResponseDTO createSpot(ParkingRequestDTO req) {
        log.debug("IN ParkingService: createSpot");
        User owner = userRepository.findById(req.getOwnerId())
                .orElseThrow(() -> new RegistrationException("Registration error, please check the data is correct"));
        User currentUser = currentUserService.getCurrentUser();
        if (currentUserService.hasRole(Role.OWNER) && owner.getId() != currentUser.getId()) {
            throw new YouAreNotOwnerException("You can only create spots for yourself");
        }
        ParkingSpot spot = parkingSpotMapper.mapFromParkingRequestDTOToParkingSpot(req);
        spot.setUser(owner);
        ParkingSpot saved = parkingRepository.save(spot);
        log.debug("OUT ParkingService: createSpot");
        return parkingSpotMapper.mapFromParkingSpotToParkingResponseDTO(saved);
    }

    public List<ParkingResponseDTO> getAllSpots() {
        releaseExpiredSpots();
        return parkingRepository.findAll()
                .stream()
                .map(parkingSpotMapper::mapFromParkingSpotToParkingResponseDTO)
                .collect(Collectors.toList());
    }


    public ParkingResponseDTO getSpotById(Integer id) {
        log.debug("IN ParkingService: getSpotById");
        ParkingSpot spot = parkingRepository.findById(id)
                .orElseThrow(() -> new SpotNotFoundException("Spot not found with id: " + id));
        log.debug("OUT ParkingService: getSpotById");
        return parkingSpotMapper.mapFromParkingSpotToParkingResponseDTO(spot);
    }

    public List<ParkingResponseDTO> getSpotByUser(Integer userId) {
        log.debug("IN ParkingService: getSpotByUser");
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        log.debug("OUT ParkingService: getSpotByUser");
        return parkingRepository.findByUserId(userId).stream()
                .map(parkingSpotMapper::mapFromParkingSpotToParkingResponseDTO)
                .collect(Collectors.toList());
    }

    public ParkingResponseDTO updateSpot(Integer spotId, ParkingRequestDTO req) {
        log.debug("IN ParkingService: updateSpot");
        ParkingSpot spot = parkingRepository.findById(spotId)
                .orElseThrow(() -> new SpotNotFoundException("Spot not found with id: " + spotId));
        verifyOwnerOrAdmin(spot);
        spot.setAddress(req.getAddress());
        spot.setDescription(req.getDescription());
        spot.setPricePerHour(req.getPricePerHour());
        spot.setStatus(req.getStatus());
        ParkingSpot updated = parkingRepository.save(spot);
        log.info("Spot with id: {} updated", spotId);
        log.debug("OUT ParkingService: updateSpot");
        return parkingSpotMapper.mapFromParkingSpotToParkingResponseDTO(updated);
    }

    public void deleteSpot(Integer spotId) {
        log.debug("IN ParkingService: deleteSpot");
        ParkingSpot spot = parkingRepository.findById(spotId)
                .orElseThrow(() -> new SpotNotFoundException("Spot not found with id: " + spotId));
        verifyOwnerOrAdmin(spot);
        parkingRepository.delete(spot);
        log.info("Delete Spot with id: {}", spotId);
        log.debug("OUT ParkingService: deleteSpot");
    }

    private void verifyOwnerOrAdmin(ParkingSpot spot) {
        if (currentUserService.hasRole(Role.ADMIN)) {
            return;
        }
        User currentUser = currentUserService.getCurrentUser();
        if (spot.getUser().getId() != currentUser.getId()) {
            throw new YouAreNotOwnerException("You are not the owner of this parking spot");
        }
    }

    private void releaseExpiredSpots() {
        LocalDateTime now = LocalDateTime.now();
        int updated = parkingRepository.releaseExpiredSpots(Status.FREE, Status.BUSY, now);
        if (updated > 0) {
            log.debug("Released {} expired spots", updated);
        }
    }
}
