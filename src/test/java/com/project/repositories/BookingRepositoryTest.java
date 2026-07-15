package com.project.repositories;

import com.project.models.Booking;
import com.project.models.ParkingSpot;
import com.project.models.Status;
import com.project.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private ParkingSpot testSpot;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        entityManager.clear();

        testUser = new User();
        testUser.setFullName("John Doe");
        testUser.setPhone("+123456789");
        testUser.setEmail("john@example.com");
        testUser.setAge(30);
        testUser.setCreated(LocalDateTime.now());
        testUser.setUpdated(LocalDateTime.now());
        entityManager.persistAndFlush(testUser);

        testSpot = new ParkingSpot();
        testSpot.setAddress("Downtown 1");
        testSpot.setDescription("Covered parking");
        testSpot.setPricePerHour(BigDecimal.valueOf(5.0));
        testSpot.setStatus(Status.FREE);
        testSpot.setUser(testUser);
        entityManager.persistAndFlush(testSpot);
    }

    private Booking createBooking(LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setRenter(testUser);
        booking.setSpot(testSpot);
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setTotalPrice(BigDecimal.valueOf(100.0));
        return booking;
    }


    @Test
    void shouldReturnFalse_whenNoBookingExistsForSpot() {
        boolean exists = bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                testSpot.getId(),
                LocalDateTime.of(2025, 1, 10, 12, 0),
                LocalDateTime.of(2025, 1, 10, 10, 0)
        );
        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnFalse_whenBookingEndsBeforeRangeStart() {
        Booking booking = createBooking(
                LocalDateTime.of(2025, 1, 10, 8, 0),
                LocalDateTime.of(2025, 1, 10, 9, 0)
        );
        entityManager.persistAndFlush(booking);

        boolean exists = bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                testSpot.getId(),
                LocalDateTime.of(2025, 1, 10, 12, 0),
                LocalDateTime.of(2025, 1, 10, 10, 0)
        );
        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnFalse_whenBookingStartsAfterRangeEnd() {
        Booking booking = createBooking(
                LocalDateTime.of(2025, 1, 10, 13, 0),
                LocalDateTime.of(2025, 1, 10, 14, 0)
        );
        entityManager.persistAndFlush(booking);

        boolean exists = bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                testSpot.getId(),
                LocalDateTime.of(2025, 1, 10, 12, 0),
                LocalDateTime.of(2025, 1, 10, 10, 0)
        );
        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnTrue_whenBookingExactlyMatchesRange() {
        Booking booking = createBooking(
                LocalDateTime.of(2025, 1, 10, 10, 0),
                LocalDateTime.of(2025, 1, 10, 12, 0)
        );
        entityManager.persistAndFlush(booking);

        boolean exists = bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                testSpot.getId(),
                LocalDateTime.of(2025, 1, 10, 12, 0),
                LocalDateTime.of(2025, 1, 10, 10, 0)
        );
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnTrue_whenBookingContainsRange() {
        Booking booking = createBooking(
                LocalDateTime.of(2025, 1, 10, 9, 0),
                LocalDateTime.of(2025, 1, 10, 13, 0)
        );
        entityManager.persistAndFlush(booking);

        boolean exists = bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                testSpot.getId(),
                LocalDateTime.of(2025, 1, 10, 12, 0),
                LocalDateTime.of(2025, 1, 10, 10, 0)
        );
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnTrue_whenBookingIsInsideRange() {
        Booking booking = createBooking(
                LocalDateTime.of(2025, 1, 10, 10, 30),
                LocalDateTime.of(2025, 1, 10, 11, 30)
        );
        entityManager.persistAndFlush(booking);

        boolean exists = bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                testSpot.getId(),
                LocalDateTime.of(2025, 1, 10, 12, 0),
                LocalDateTime.of(2025, 1, 10, 10, 0)
        );
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnTrue_whenBookingOverlapsStartOfRange() {
        Booking booking = createBooking(
                LocalDateTime.of(2025, 1, 10, 9, 30),
                LocalDateTime.of(2025, 1, 10, 10, 30)
        );
        entityManager.persistAndFlush(booking);

        boolean exists = bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                testSpot.getId(),
                LocalDateTime.of(2025, 1, 10, 12, 0),
                LocalDateTime.of(2025, 1, 10, 10, 0)
        );
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnTrue_whenBookingOverlapsEndOfRange() {
        Booking booking = createBooking(
                LocalDateTime.of(2025, 1, 10, 11, 30),
                LocalDateTime.of(2025, 1, 10, 12, 30)
        );
        entityManager.persistAndFlush(booking);

        boolean exists = bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                testSpot.getId(),
                LocalDateTime.of(2025, 1, 10, 12, 0),
                LocalDateTime.of(2025, 1, 10, 10, 0)
        );
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnTrue_whenBookingEndsExactlyAtRangeStart() {
        Booking booking = createBooking(
                LocalDateTime.of(2025, 1, 10, 8, 0),
                LocalDateTime.of(2025, 1, 10, 10, 0)
        );
        entityManager.persistAndFlush(booking);

        boolean exists = bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                testSpot.getId(),
                LocalDateTime.of(2025, 1, 10, 12, 0),
                LocalDateTime.of(2025, 1, 10, 10, 0)
        );
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnTrue_whenBookingStartsExactlyAtRangeEnd() {
        Booking booking = createBooking(
                LocalDateTime.of(2025, 1, 10, 12, 0),
                LocalDateTime.of(2025, 1, 10, 14, 0)
        );
        entityManager.persistAndFlush(booking);

        boolean exists = bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                testSpot.getId(),
                LocalDateTime.of(2025, 1, 10, 12, 0),
                LocalDateTime.of(2025, 1, 10, 10, 0)
        );
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_whenBookingExistsForDifferentSpot() {
        ParkingSpot otherSpot = new ParkingSpot();
        otherSpot.setAddress("Another address");
        otherSpot.setDescription("Open parking");
        otherSpot.setPricePerHour(BigDecimal.valueOf(3.0));
        otherSpot.setStatus(Status.FREE);
        otherSpot.setUser(testUser);
        entityManager.persistAndFlush(otherSpot);

        Booking booking = new Booking();
        booking.setRenter(testUser);
        booking.setSpot(otherSpot);
        booking.setStartTime(LocalDateTime.of(2025, 1, 10, 10, 0));
        booking.setEndTime(LocalDateTime.of(2025, 1, 10, 12, 0));
        booking.setTotalPrice(BigDecimal.valueOf(50.0));
        entityManager.persistAndFlush(booking);

        boolean exists = bookingRepository.existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                testSpot.getId(),
                LocalDateTime.of(2025, 1, 10, 12, 0),
                LocalDateTime.of(2025, 1, 10, 10, 0)
        );
        assertThat(exists).isFalse();
    }
}
