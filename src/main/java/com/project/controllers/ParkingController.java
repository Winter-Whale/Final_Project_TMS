package com.project.controllers;

import com.project.models.dto.parking.ParkingRequestDTO;
import com.project.models.dto.parking.ParkingResponseDTO;
import com.project.services.ParkingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Parking spots", description = "Parking spots management")
public class ParkingController {

    private final ParkingService parkingService;

    @GetMapping
    @Operation(summary = "Get all parking spots",
            description = "Returns a list of all spots with their status (Free/Busy).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The list has been received.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ParkingResponseDTO.class)))
    })
    public ResponseEntity<List<ParkingResponseDTO>> getAllSpots() {
        return ResponseEntity.ok(parkingService.getAllSpots());
    }

    @GetMapping("/spot/{id}")
    @Operation(summary = "Get a spot by ID",
            description = "Returns information about a specific parking spot.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The spot has been found.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ParkingResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Spot not found",
                    content = @Content)
    })
    public ResponseEntity<ParkingResponseDTO> getSpotById(
            @Parameter(description = "ID parking spot", required = true, example = "1")
            @PathVariable Integer id) {
        ParkingResponseDTO responseDTO = parkingService.getSpotById(id);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/user/{id}")
    @Operation(summary = "Get all user spots",
            description = "Returns a list of spots owned by the user with the specified ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The list has been received.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ParkingResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content)
    })
    public ResponseEntity<List<ParkingResponseDTO>> getSpotByUser(
            @Parameter(description = "User ID", required = true, example = "2")
            @PathVariable Integer id) {
        return ResponseEntity.ok(parkingService.getSpotByUser(id));
    }

    @PostMapping
    @Operation(summary = "Create a parking spot",
            description = "The owner or administrator creates a new spot. This feature is only available to the OWNER and ADMIN roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The spot has been created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ParkingResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Incorrect data"),
            @ApiResponse(responseCode = "403", description = "Access denied (not OWNER/ADMIN)")
    })
    public ResponseEntity<ParkingResponseDTO> createSpot(
            @Parameter(description = "Data for creating a spot", required = true)
            @RequestBody @Valid ParkingRequestDTO requestDTO) {
        ParkingResponseDTO createSpot = parkingService.createSpot(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createSpot);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a parking spot",
            description = "Updates information about a spot. Available to the spot owner or administrator.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The spot has been updated.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ParkingResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient rights"),
            @ApiResponse(responseCode = "404", description = "Spot not found")
    })
    public ResponseEntity<ParkingResponseDTO> updateSpot(
            @Parameter(description = "Spot ID", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(description = "New spot data", required = true)
            @RequestBody @Valid ParkingRequestDTO requestDTO) {
        ParkingResponseDTO update = parkingService.updateSpot(id, requestDTO);
        return ResponseEntity.ok(update);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete parking spot",
            description = "Deletes a spot by ID. Available to owner or administrator.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The spot has been removed."),
            @ApiResponse(responseCode = "403", description = "Insufficient rights"),
            @ApiResponse(responseCode = "404", description = "Spot not found")
    })
    public ResponseEntity<Void> deleteSpot(
            @Parameter(description = "Spot ID", required = true, example = "1")
            @PathVariable Integer id) {
        parkingService.deleteSpot(id);
        return ResponseEntity.noContent().build();
    }
}
