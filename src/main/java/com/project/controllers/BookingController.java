package com.project.controllers;

import com.project.models.dto.booking.BookingRequestDTO;
import com.project.models.dto.booking.BookingResponseDTO;
import com.project.services.BookingService;
import com.project.services.CurrentUserService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
@Tag(name = "Bookings", description = "Parking space reservation management")
public class BookingController {

    private final BookingService bookingService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @Operation(summary = "Create a booking",
            description = "The renter creates a reservation for a specific parking space for a selected time interval.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BookingResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data (spot occupied, invalid time, etc.)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Parking spot or user not found",
                    content = @Content)
    })
    public ResponseEntity<BookingResponseDTO> createBooking(
            @Parameter(description = "Booking details", required = true)
            @RequestBody @Valid BookingRequestDTO req) {
        int renterId = currentUserService.getCurrentUser().getId();
        BookingResponseDTO responseDTO = bookingService.createBooking(req, renterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
