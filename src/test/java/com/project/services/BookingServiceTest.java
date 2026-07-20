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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ParkingRepository parkingRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private BookingService bookingService;

    private final Integer RENTER_ID = 1;
    private final Integer SPOT_ID = 100;
    private final String ADDRESS = "Test Address";
    private final BigDecimal PRICE_PER_HOUR = BigDecimal.valueOf(10.0);

    private User renter;
    private ParkingSpot spot;
    private BookingRequestDTO validRequest;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        renter = new User();
        renter.setId(RENTER_ID);
        renter.setFullName("John Doe");

        spot = new ParkingSpot();
        spot.setId(SPOT_ID);
        spot.setAddress(ADDRESS);
        spot.setPricePerHour(PRICE_PER_HOUR);
        spot.setStatus(Status.FREE);

        validRequest = new BookingRequestDTO();
        validRequest.setSpotId(SPOT_ID);
        validRequest.setStartTime(now.plusHours(2));
        validRequest.setEndTime(now.plusHours(5));
    }

    @Test
    void shouldCreateBookingSuccessfully() {
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.of(spot));
        when(bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(userService.getUserById(RENTER_ID)).thenReturn(Optional.of(renter));

        Booking savedBooking = new Booking();
        savedBooking.setId(1);
        savedBooking.setSpot(spot);
        savedBooking.setRenter(renter);
        savedBooking.setStartTime(validRequest.getStartTime());
        savedBooking.setEndTime(validRequest.getEndTime());
        long hours = ChronoUnit.HOURS.between(validRequest.getStartTime(), validRequest.getEndTime());
        if (hours < 1) hours = 1;
        savedBooking.setTotalPrice(PRICE_PER_HOUR.multiply(BigDecimal.valueOf(hours)));
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingResponseDTO response = bookingService.createBooking(validRequest, RENTER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getBookingId()).isEqualTo(1);
        assertThat(response.getSpotId()).isEqualTo(SPOT_ID);
        assertThat(response.getAddress()).isEqualTo(ADDRESS);
        assertThat(response.getTotalPrice()).isEqualTo(PRICE_PER_HOUR.multiply(BigDecimal.valueOf(3)));
        assertThat(response.getSpotStatus()).isEqualTo(Status.BUSY);

        ArgumentCaptor<ParkingSpot> spotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
        verify(parkingRepository).save(spotCaptor.capture());
        assertThat(spotCaptor.getValue().getStatus()).isEqualTo(Status.BUSY);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void shouldThrowSpotNotFoundException_whenSpotNotFound() {
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(validRequest, RENTER_ID))
                .isInstanceOf(SpotNotFoundException.class)
                .hasMessageContaining("Spot not found");
        verify(parkingRepository, never()).save(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldThrowBookingException_whenSpotNotFree() {
        spot.setStatus(Status.BUSY);
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.of(spot));

        assertThatThrownBy(() -> bookingService.createBooking(validRequest, RENTER_ID))
                .isInstanceOf(BookingException.class)
                .hasMessage("Spot is not available for booking");
        verify(bookingRepository, never()).existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(anyInt(), any(), any());
        verify(parkingRepository, never()).save(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldThrowBookingException_whenOverlappingBookingExists() {
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.of(spot));
        when(bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(validRequest, RENTER_ID))
                .isInstanceOf(BookingException.class)
                .hasMessage("Spot is already booked for this time period");
        verify(parkingRepository, never()).save(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldThrowBookingException_whenStartTimeAfterEndTime() {
        validRequest.setStartTime(now.plusHours(5));
        validRequest.setEndTime(now.plusHours(2));
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.of(spot));
        when(bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> bookingService.createBooking(validRequest, RENTER_ID))
                .isInstanceOf(BookingException.class)
                .hasMessage("Start time must be before end time");
        verify(parkingRepository, never()).save(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldThrowBookingException_whenStartTimeInPast() {
        validRequest.setStartTime(now.minusHours(1));
        validRequest.setEndTime(now.plusHours(2));
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.of(spot));
        when(bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> bookingService.createBooking(validRequest, RENTER_ID))
                .isInstanceOf(BookingException.class)
                .hasMessage("Start time must be in future");
        verify(parkingRepository, never()).save(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldThrowUserNotFoundException_whenUserNotFound() {
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.of(spot));
        when(bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(userService.getUserById(RENTER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(validRequest, RENTER_ID))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with id: " + RENTER_ID);
        verify(parkingRepository, never()).save(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldRoundUpToMinimumOneHour_whenBookingLessThanHour() {
        validRequest.setStartTime(now.plusHours(1));
        validRequest.setEndTime(now.plusHours(1).plusMinutes(30));
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.of(spot));
        when(bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(userService.getUserById(RENTER_ID)).thenReturn(Optional.of(renter));

        Booking savedBooking = new Booking();
        savedBooking.setId(2);
        savedBooking.setSpot(spot);
        savedBooking.setRenter(renter);
        savedBooking.setStartTime(validRequest.getStartTime());
        savedBooking.setEndTime(validRequest.getEndTime());
        savedBooking.setTotalPrice(PRICE_PER_HOUR);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingResponseDTO response = bookingService.createBooking(validRequest, RENTER_ID);

        assertThat(response.getTotalPrice()).isEqualTo(PRICE_PER_HOUR);
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertThat(bookingCaptor.getValue().getTotalPrice()).isEqualTo(PRICE_PER_HOUR);
    }

    @Test
    void shouldCalculatePriceForMultipleHoursCorrectly() {
        validRequest.setStartTime(now.plusHours(2));
        validRequest.setEndTime(now.plusHours(7));
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.of(spot));
        when(bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(userService.getUserById(RENTER_ID)).thenReturn(Optional.of(renter));

        Booking savedBooking = new Booking();
        savedBooking.setId(3);
        savedBooking.setSpot(spot);
        savedBooking.setRenter(renter);
        savedBooking.setStartTime(validRequest.getStartTime());
        savedBooking.setEndTime(validRequest.getEndTime());
        savedBooking.setTotalPrice(PRICE_PER_HOUR.multiply(BigDecimal.valueOf(5)));
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingResponseDTO response = bookingService.createBooking(validRequest, RENTER_ID);

        assertThat(response.getTotalPrice()).isEqualTo(PRICE_PER_HOUR.multiply(BigDecimal.valueOf(5)));
    }

    @Test
    void shouldCallAllRequiredMethodsInCorrectOrder() {
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.of(spot));
        when(bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                anyInt(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(userService.getUserById(RENTER_ID)).thenReturn(Optional.of(renter));

        Booking savedBooking = new Booking();
        savedBooking.setId(5);
        savedBooking.setSpot(spot);
        savedBooking.setRenter(renter);
        savedBooking.setStartTime(validRequest.getStartTime());
        savedBooking.setEndTime(validRequest.getEndTime());
        savedBooking.setTotalPrice(BigDecimal.TEN);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        bookingService.createBooking(validRequest, RENTER_ID);

        var inOrder = Mockito.inOrder(parkingRepository, bookingRepository, userService);
        inOrder.verify(parkingRepository).findById(SPOT_ID);
        inOrder.verify(bookingRepository).existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                anyInt(), any(LocalDateTime.class), any(LocalDateTime.class));
        inOrder.verify(userService).getUserById(RENTER_ID);
        inOrder.verify(bookingRepository).save(any(Booking.class));
        inOrder.verify(parkingRepository).save(any(ParkingSpot.class));
    }
}