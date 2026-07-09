package com.project.controllers;

import com.project.models.dto.Parking.ParkingRequestDTO;
import com.project.models.dto.Parking.ParkingResponseDTO;
import com.project.services.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/parking")
public class ParkingController {

    private final ParkingService parkingService;

    @GetMapping
    public ResponseEntity<List<ParkingResponseDTO>> getAllSpots() {
        return ResponseEntity.ok(parkingService.getAllSpots());
    }

    @GetMapping("/spot/{id}")
    public ResponseEntity<ParkingResponseDTO> getSpotById(@PathVariable Integer id) {
        ParkingResponseDTO responseDTO = parkingService.getSpotById(id);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<ParkingResponseDTO>> getSpotByUser(@PathVariable Integer id) {
        return ResponseEntity.ok(parkingService.getSpotByUser(id));
    }

    @PostMapping
    public ResponseEntity<ParkingResponseDTO> createSpot(@RequestBody @Valid ParkingRequestDTO requestDTO) {
        ParkingResponseDTO createSpot = parkingService.createSpot(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createSpot);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParkingResponseDTO> updateSpot(
            @PathVariable Integer id,
            @RequestBody @Valid ParkingRequestDTO requestDTO) {
        ParkingResponseDTO update = parkingService.updateSpot(id, requestDTO);
        return ResponseEntity.ok(update);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpot(@PathVariable Integer id) {
        parkingService.deleteSpot(id);
        return ResponseEntity.noContent().build();
    }
}
