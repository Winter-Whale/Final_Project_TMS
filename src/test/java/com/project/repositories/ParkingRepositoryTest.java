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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
public class ParkingRepositoryTest {

    @Autowired
    private ParkingRepository parkingRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        parkingRepository.deleteAll();
        entityManager.clear();
    }

    private User createUser(String fullName, String phone, String email, int age) {
        User user = new User();
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setAge(age);
        user.setCreated(LocalDateTime.now());
        user.setUpdated(LocalDateTime.now());
        return user;
    }

    private ParkingSpot createParkingSpot(User owner, String address, String description,
                                          BigDecimal price, Status status) {
        ParkingSpot spot = new ParkingSpot();
        spot.setAddress(address);
        spot.setDescription(description);
        spot.setPricePerHour(price);
        spot.setStatus(status);
        spot.setUser(owner);
        return spot;
    }

    private Booking createBooking(ParkingSpot spot, User renter,
                                  LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setSpot(spot);
        booking.setRenter(renter);
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setTotalPrice(BigDecimal.valueOf(100.0));
        return booking;
    }

    @Test
    void shouldFindParkingSpotsByUserId() {
        User user1 = createUser("John Doe", "+123456789", "john@example.com", 30);
        User user2 = createUser("Jane Smith", "+987654321", "jane@example.com", 25);
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        ParkingSpot spot1 = createParkingSpot(user1, "Addr1", "Desc1", BigDecimal.valueOf(5), Status.BUSY);
        ParkingSpot spot2 = createParkingSpot(user1, "Addr2", "Desc2", BigDecimal.valueOf(6), Status.FREE);
        ParkingSpot spot3 = createParkingSpot(user1, "Addr3", "Desc3", BigDecimal.valueOf(4), Status.BUSY);
        ParkingSpot spot4 = createParkingSpot(user2, "Addr4", "Desc4", BigDecimal.valueOf(7), Status.FREE);
        entityManager.persist(spot1);
        entityManager.persist(spot2);
        entityManager.persist(spot3);
        entityManager.persist(spot4);
        entityManager.flush();

        List<ParkingSpot> spotsOfUser1 = parkingRepository.findByUserId(user1.getId());
        List<ParkingSpot> spotsOfUser2 = parkingRepository.findByUserId(user2.getId());

        assertThat(spotsOfUser1).hasSize(3)
                .extracting(ParkingSpot::getId)
                .containsExactlyInAnyOrder(spot1.getId(), spot2.getId(), spot3.getId());

        assertThat(spotsOfUser2).hasSize(1)
                .extracting(ParkingSpot::getId)
                .containsExactly(spot4.getId());
    }

    @Test
    void shouldReturnEmptyList_whenUserHasNoSpots() {
        User user = createUser("No Spots", "+000000000", "nospots@example.com", 40);
        entityManager.persist(user);
        entityManager.flush();

        List<ParkingSpot> spots = parkingRepository.findByUserId(user.getId());
        assertThat(spots).isEmpty();
    }

    @Test
    void shouldReleaseExpiredSpots_withExpiredBookings() {
        User user = createUser("Owner", "+111", "owner@example.com", 30);
        entityManager.persist(user);
        entityManager.flush();

        ParkingSpot spot1 = createParkingSpot(user, "Addr1", "Desc1", BigDecimal.TEN, Status.BUSY);
        entityManager.persist(spot1);
        entityManager.flush();
        Booking booking1 = createBooking(spot1, user,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1));
        entityManager.persist(booking1);
        entityManager.flush();

        ParkingSpot spot2 = createParkingSpot(user, "Addr2", "Desc2", BigDecimal.TEN, Status.BUSY);
        entityManager.persist(spot2);
        entityManager.flush();
        Booking booking2 = createBooking(spot2, user,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        entityManager.persist(booking2);
        entityManager.flush();

        ParkingSpot spot3 = createParkingSpot(user, "Addr3", "Desc3", BigDecimal.TEN, Status.FREE);
        entityManager.persist(spot3);
        entityManager.flush();
        Booking booking3 = createBooking(spot3, user,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2));
        entityManager.persist(booking3);
        entityManager.flush();

        ParkingSpot spot4 = createParkingSpot(user, "Addr4", "Desc4", BigDecimal.TEN, Status.BUSY);
        entityManager.persist(spot4);
        entityManager.flush();
        Booking booking4 = createBooking(spot4, user,
                LocalDateTime.now().minusDays(4),
                LocalDateTime.now().minusDays(3));
        entityManager.persist(booking4);
        entityManager.flush();

        LocalDateTime now = LocalDateTime.now();
        int updatedCount = parkingRepository.releaseExpiredSpots(Status.FREE, Status.BUSY, now);

        assertThat(updatedCount).isEqualTo(2);
        entityManager.clear();

        ParkingSpot reloaded1 = entityManager.find(ParkingSpot.class, spot1.getId());
        ParkingSpot reloaded2 = entityManager.find(ParkingSpot.class, spot2.getId());
        ParkingSpot reloaded3 = entityManager.find(ParkingSpot.class, spot3.getId());
        ParkingSpot reloaded4 = entityManager.find(ParkingSpot.class, spot4.getId());

        assertThat(reloaded1.getStatus()).isEqualTo(Status.FREE);
        assertThat(reloaded2.getStatus()).isEqualTo(Status.BUSY);
        assertThat(reloaded3.getStatus()).isEqualTo(Status.FREE); // не изменился
        assertThat(reloaded4.getStatus()).isEqualTo(Status.FREE);
    }

    @Test
    void shouldNotUpdateAnySpot_whenNoExpiredBookings() {
        User user = createUser("User", "+222", "user@example.com", 25);
        entityManager.persist(user);
        entityManager.flush();

        ParkingSpot spot = createParkingSpot(user, "Addr", "Desc", BigDecimal.TEN, Status.BUSY);
        entityManager.persist(spot);
        entityManager.flush();

        Booking futureBooking = createBooking(spot, user,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        entityManager.persist(futureBooking);
        entityManager.flush();

        LocalDateTime now = LocalDateTime.now();
        int updatedCount = parkingRepository.releaseExpiredSpots(Status.FREE, Status.BUSY, now);

        assertThat(updatedCount).isEqualTo(0);

        entityManager.clear();
        ParkingSpot reloaded = entityManager.find(ParkingSpot.class, spot.getId());
        assertThat(reloaded.getStatus()).isEqualTo(Status.BUSY);
    }

    @Test
    void shouldReturnZero_whenNoSpotsMatchOldStatus() {
        User user = createUser("FreeOwner", "+333", "free@example.com", 30);
        entityManager.persist(user);
        entityManager.flush();

        ParkingSpot spot = createParkingSpot(user, "Addr", "Desc", BigDecimal.TEN, Status.FREE);
        entityManager.persist(spot);
        entityManager.flush();

        Booking expired = createBooking(spot, user,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1));
        entityManager.persist(expired);
        entityManager.flush();

        LocalDateTime now = LocalDateTime.now();
        int updatedCount = parkingRepository.releaseExpiredSpots(Status.FREE, Status.BUSY, now);

        assertThat(updatedCount).isEqualTo(0);

        entityManager.clear();
        ParkingSpot reloaded = entityManager.find(ParkingSpot.class, spot.getId());
        assertThat(reloaded.getStatus()).isEqualTo(Status.FREE);
    }

    @Test
    void shouldNotUpdateSpotsWithWrongOldStatus() {
        User user = createUser("Test", "+444", "test@example.com", 20);
        entityManager.persist(user);
        entityManager.flush();

        ParkingSpot spot = createParkingSpot(user, "Addr", "Desc", BigDecimal.TEN, Status.BUSY);
        entityManager.persist(spot);
        entityManager.flush();

        Booking expired = createBooking(spot, user,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1));
        entityManager.persist(expired);
        entityManager.flush();

        LocalDateTime now = LocalDateTime.now();
        int updatedCount = parkingRepository.releaseExpiredSpots(Status.FREE, Status.FREE, now);

        assertThat(updatedCount).isEqualTo(0);

        entityManager.clear();
        ParkingSpot reloaded = entityManager.find(ParkingSpot.class, spot.getId());
        assertThat(reloaded.getStatus()).isEqualTo(Status.BUSY);
    }
}