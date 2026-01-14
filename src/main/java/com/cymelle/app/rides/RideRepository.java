package com.cymelle.app.rides;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideRepository extends JpaRepository<Ride, Long> {

    Page<Ride> findByUserId(Long userId, Pageable pageable);

    Page<Ride> findByUserIdAndStatus(Long userId, RideStatus status, Pageable pageable);

    Page<Ride> findByStatus(RideStatus status, Pageable pageable);

    Page<Ride> findByDriverId(Long driverId, Pageable pageable);

    Page<Ride> findByDriverIdAndStatus(Long driverId, RideStatus status, Pageable pageable);
}
