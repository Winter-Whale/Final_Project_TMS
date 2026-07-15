package com.project.repositories;

import com.project.models.ParkingSpot;
import com.project.models.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;

public interface ParkingRepository extends JpaRepository<ParkingSpot, Integer> {
    List<ParkingSpot> findByUserId(Integer userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ParkingSpot p SET p.status = :newStatus WHERE p.status = :oldStatus AND EXISTS " +
            "(SELECT b FROM Booking b WHERE b.spot = p AND b.endTime < :now)")
    int releaseExpiredSpots(@Param("newStatus") Status newStatus,
                            @Param("oldStatus") Status oldStatus,
                            @Param("now") LocalDateTime now);
}
