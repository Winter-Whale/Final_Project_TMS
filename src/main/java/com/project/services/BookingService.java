package com.project.services;

import com.project.exceptions.BookingException;
import com.project.exceptions.SpotNotFoundException;
import com.project.exceptions.UserNotFoundException;
import com.project.models.Booking;
import com.project.models.ParkingSpot;
import com.project.models.Status;
import com.project.models.User;
import com.project.models.dto.booking.BookingRequestDTO;
import com.project.models.dto.booking.BookingResponseDTO;
import com.project.repositories.BookingRepository;
import com.project.repositories.ParkingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {
    private final BookingRepository bookingRepository;
    private final ParkingRepository parkingRepository;
    private final UserService userService;

    public BookingResponseDTO createBooking(BookingRequestDTO dto, Integer renterId) {
        log.debug("IN BookingService: createBooking");
        ParkingSpot spot = parkingRepository.findById(dto.getSpotId())
                .orElseThrow(() -> new SpotNotFoundException("Spot not found"));
        if (spot.getStatus() != Status.FREE) {
            throw new BookingException("Spot is not available for booking");
        }

        boolean overlapping = bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                spot.getId(), dto.getEndTime(), dto.getStartTime());
        if (overlapping) {
            throw new BookingException("Spot is already booked for this time period");
        }
        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new BookingException("Start time must be before end time");
        }
        if (dto.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BookingException("Start time must be in future");
        }
        long hours = ChronoUnit.HOURS.between(dto.getStartTime(), dto.getEndTime());
        if (hours < 1) hours = 1;
        BigDecimal total = spot.getPricePerHour().multiply(BigDecimal.valueOf(hours));

        User renter = userService.getUserById(renterId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + renterId));

        Booking booking = new Booking();
        booking.setSpot(spot);
        booking.setRenter(renter);
        booking.setStartTime(dto.getStartTime());
        booking.setEndTime(dto.getEndTime());
        booking.setTotalPrice(total);
        Booking saved = bookingRepository.save(booking);

        spot.setStatus(Status.BUSY);
        parkingRepository.save(spot);
        log.debug("OUT BookingService: createBooking");
        return mapToResponse(saved);
    }

    private BookingResponseDTO mapToResponse(Booking booking) {
        log.debug("IN BookingService: mapToResponse");
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setBookingId(booking.getId());
        dto.setSpotId(booking.getSpot().getId());
        dto.setAddress(booking.getSpot().getAddress());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setSpotStatus(booking.getSpot().getStatus());
        log.debug("OUT BookingService: mapToResponse");
        return dto;
    }
}
