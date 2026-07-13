package com.LocalServe.LocalServe.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED, COMPLETED

    private Double price;
    
    @Column(length = 500)
    private String notes;

    // No-arg constructor
    public Booking() {
    }

    // All-arg constructor
    public Booking(Long id, User customer, Vendor vendor, LocalDate bookingDate, String status, Double price, String notes) {
        this.id = id;
        this.customer = customer;
        this.vendor = vendor;
        this.bookingDate = bookingDate;
        this.status = status;
        this.price = price;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Manual Builder
    public static BookingBuilder builder() {
        return new BookingBuilder();
    }

    public static class BookingBuilder {
        private Long id;
        private User customer;
        private Vendor vendor;
        private LocalDate bookingDate;
        private String status;
        private Double price;
        private String notes;

        public BookingBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BookingBuilder customer(User customer) {
            this.customer = customer;
            return this;
        }

        public BookingBuilder vendor(Vendor vendor) {
            this.vendor = vendor;
            return this;
        }

        public BookingBuilder bookingDate(LocalDate bookingDate) {
            this.bookingDate = bookingDate;
            return this;
        }

        public BookingBuilder status(String status) {
            this.status = status;
            return this;
        }

        public BookingBuilder price(Double price) {
            this.price = price;
            return this;
        }

        public BookingBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Booking build() {
            return new Booking(id, customer, vendor, bookingDate, status, price, notes);
        }
    }
}
