package com.LocalServe.LocalServe.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "emergency_offers")
public class EmergencyOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id", nullable = false)
    private EmergencyRequest request;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(nullable = false)
    private Double emergencyCharge;

    @Column(nullable = false)
    private String arrivalTime; // e.g. "30 mins", "1 hour"

    @Column(length = 1000)
    private String message;

    @Column(nullable = false)
    private String status; // PENDING, ACCEPTED, REJECTED, CLOSED

    public EmergencyOffer() {
    }

    public EmergencyOffer(EmergencyRequest request, Vendor vendor, Double emergencyCharge, String arrivalTime, String message) {
        this.request = request;
        this.vendor = vendor;
        this.emergencyCharge = emergencyCharge;
        this.arrivalTime = arrivalTime;
        this.message = message;
        this.status = "PENDING";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EmergencyRequest getRequest() {
        return request;
    }

    public void setRequest(EmergencyRequest request) {
        this.request = request;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public Double getEmergencyCharge() {
        return emergencyCharge;
    }

    public void setEmergencyCharge(Double emergencyCharge) {
        this.emergencyCharge = emergencyCharge;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
