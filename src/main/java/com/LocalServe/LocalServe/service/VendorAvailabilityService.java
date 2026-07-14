package com.LocalServe.LocalServe.service;

import com.LocalServe.LocalServe.entity.Booking;
import com.LocalServe.LocalServe.entity.Vendor;
import com.LocalServe.LocalServe.entity.VendorAvailability;
import com.LocalServe.LocalServe.repository.BookingRepository;
import com.LocalServe.LocalServe.repository.VendorAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class VendorAvailabilityService {

    @Autowired
    private VendorAvailabilityRepository availabilityRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public void setAvailability(Vendor vendor, LocalDate date, String status) {
        Optional<VendorAvailability> existing = availabilityRepository.findByVendorAndAvailabilityDate(vendor, date);
        if (existing.isPresent()) {
            VendorAvailability va = existing.get();
            if ("AVAILABLE".equalsIgnoreCase(status) || status == null || status.trim().isEmpty()) {
                availabilityRepository.delete(va);
            } else {
                va.setStatus(status.toUpperCase());
                availabilityRepository.save(va);
            }
        } else {
            if (status != null && !status.trim().isEmpty() && !"AVAILABLE".equalsIgnoreCase(status)) {
                VendorAvailability va = new VendorAvailability(vendor, date, status.toUpperCase());
                availabilityRepository.save(va);
            }
        }
    }

    public List<VendorAvailability> getAvailabilityForRange(Vendor vendor, LocalDate start, LocalDate end) {
        return availabilityRepository.findByVendorAndAvailabilityDateBetween(vendor, start, end);
    }

    public String resolveDateStatus(Vendor vendor, LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            return "PAST";
        }

        // Check if there is an active booking on this date
        boolean hasBooking = bookingRepository.findByVendorOrderByBookingDateDesc(vendor).stream()
                .anyMatch(b -> b.getBookingDate().equals(date) && 
                        ("APPROVED".equalsIgnoreCase(b.getStatus()) || "COMPLETED".equalsIgnoreCase(b.getStatus()) || "PENDING".equalsIgnoreCase(b.getStatus())));

        if (hasBooking) {
            return "BOOKED";
        }

        // Check custom availability override
        Optional<VendorAvailability> custom = availabilityRepository.findByVendorAndAvailabilityDate(vendor, date);
        if (custom.isPresent()) {
            return custom.get().getStatus(); // UNAVAILABLE, BLOCKED
        }

        return "AVAILABLE";
    }
}
