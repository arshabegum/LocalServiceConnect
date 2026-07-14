package com.LocalServe.LocalServe.repository;

import com.LocalServe.LocalServe.entity.Vendor;
import com.LocalServe.LocalServe.entity.VendorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorAvailabilityRepository extends JpaRepository<VendorAvailability, Long> {
    List<VendorAvailability> findByVendorAndAvailabilityDateBetween(Vendor vendor, LocalDate start, LocalDate end);
    Optional<VendorAvailability> findByVendorAndAvailabilityDate(Vendor vendor, LocalDate date);
}
