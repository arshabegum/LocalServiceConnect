package com.LocalServe.LocalServe.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vendor_availabilities")
public class VendorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(nullable = false)
    private LocalDate availabilityDate;

    @Column(nullable = false)
    private String status; // AVAILABLE, UNAVAILABLE, BLOCKED

    public VendorAvailability() {
    }

    public VendorAvailability(Vendor vendor, LocalDate availabilityDate, String status) {
        this.vendor = vendor;
        this.availabilityDate = availabilityDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public LocalDate getAvailabilityDate() {
        return availabilityDate;
    }

    public void setAvailabilityDate(LocalDate availabilityDate) {
        this.availabilityDate = availabilityDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
