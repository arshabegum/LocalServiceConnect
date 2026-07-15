package com.LocalServe.LocalServe.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "emergency_requests")
public class EmergencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false)
    private String serviceCategory; // Photographer, Caterer, etc.

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(nullable = false)
    private String eventTime;

    @Column(nullable = false)
    private String eventLocation;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private String status; // PENDING, ACCEPTED, DECLINED, EXPIRED, CONFIRMED

    private Double budget;

    public EmergencyRequest() {
    }

    public EmergencyRequest(User customer, String serviceCategory, LocalDate eventDate, String eventTime, String eventLocation, String notes, Double budget) {
        this.customer = customer;
        this.serviceCategory = serviceCategory;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventLocation = eventLocation;
        this.notes = notes;
        this.status = "PENDING";
        this.budget = budget;
    }

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

    public String getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(String serviceCategory) {
        this.serviceCategory = serviceCategory;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }
}
