package com.cymelle.app.rides;

import com.cymelle.app.users.AppUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "rides",
        indexes = {
                @Index(name = "idx_rides_user_status", columnList = "user_id,status"),
                @Index(name = "idx_rides_driver_status", columnList = "driver_id,status"),
                @Index(name = "idx_rides_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private AppUser driver;

    @Column(name = "pickup_location", nullable = false, length = 255)
    private String pickupLocation;

    @Column(name = "dropoff_location", nullable = false, length = 255)
    private String dropoffLocation;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RideStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static Ride create(
            AppUser user,
            AppUser driver,
            String pickupLocation,
            String dropoffLocation,
            BigDecimal fare
    ) {
        Ride ride = new Ride();
        ride.user = user;
        ride.driver = driver;
        ride.pickupLocation = pickupLocation;
        ride.dropoffLocation = dropoffLocation;
        ride.fare = fare;
        ride.status = RideStatus.REQUESTED;
        return ride;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        if (this.status == null) this.status = RideStatus.REQUESTED;
    }
}
