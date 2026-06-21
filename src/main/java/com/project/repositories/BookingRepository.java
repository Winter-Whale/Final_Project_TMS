package com.project.repositories;

import com.project.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    boolean existsBySpotIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            Integer spotId, LocalDateTime endTime, LocalDateTime startTime);
}
