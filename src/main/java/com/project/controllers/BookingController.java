package com.project.controllers;

import com.project.exceptions.UserNotFoundException;
import com.project.models.dto.Booking.BookingRequestDTO;
import com.project.models.dto.Booking.BookingResponseDTO;
import com.project.services.BookingService;
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

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@RequestBody @Valid BookingRequestDTO req) throws UserNotFoundException {
        BookingResponseDTO responseDTO = bookingService.createBooking(req, req.getRenterId());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
