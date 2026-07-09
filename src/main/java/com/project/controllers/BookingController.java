package com.project.controllers;

import com.project.models.dto.Booking.BookingRequestDTO;
import com.project.models.dto.Booking.BookingResponseDTO;
import com.project.services.BookingService;
import com.project.services.CurrentUserService;
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
public class BookingController {

    private final BookingService bookingService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@RequestBody @Valid BookingRequestDTO req) {
        int renterId = currentUserService.getCurrentUser().getId();
        BookingResponseDTO responseDTO = bookingService.createBooking(req, renterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
