package com.LocalServe.LocalServe.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vendors")
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String businessName;
    private String serviceType; // Photographer, Caterer, Decorator, DJ, Makeup Artist

    @Column(length = 1000)
    private String description;

    private Double price;
    private String city;

    private Boolean availabilityStatus = true;
    private Double rating = 0.0;
    private Integer totalReviews = 0;

    // No-arg constructor
    public Vendor() {
    }

    // All-arg constructor
    public Vendor(Long id, User user, String businessName, String serviceType, String description, Double price, String city, Boolean availabilityStatus, Double rating, Integer totalReviews) {
        this.id = id;
        this.user = user;
        this.businessName = businessName;
        this.serviceType = serviceType;
        this.description = description;
        this.price = price;
        this.city = city;
        this.availabilityStatus = availabilityStatus;
        this.rating = rating;
        this.totalReviews = totalReviews;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Boolean getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(Boolean availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    // Manual Builder
    public static VendorBuilder builder() {
        return new VendorBuilder();
    }

    public static class VendorBuilder {
        private Long id;
        private User user;
        private String businessName;
        private String serviceType;
        private String description;
        private Double price;
        private String city;
        private Boolean availabilityStatus = true; // default value
        private Double rating = 0.0; // default value
        private Integer totalReviews = 0; // default value

        public VendorBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public VendorBuilder user(User user) {
            this.user = user;
            return this;
        }

        public VendorBuilder businessName(String businessName) {
            this.businessName = businessName;
            return this;
        }

        public VendorBuilder serviceType(String serviceType) {
            this.serviceType = serviceType;
            return this;
        }

        public VendorBuilder description(String description) {
            this.description = description;
            return this;
        }

        public VendorBuilder price(Double price) {
            this.price = price;
            return this;
        }

        public VendorBuilder city(String city) {
            this.city = city;
            return this;
        }

        public VendorBuilder availabilityStatus(Boolean availabilityStatus) {
            this.availabilityStatus = availabilityStatus;
            return this;
        }

        public VendorBuilder rating(Double rating) {
            this.rating = rating;
            return this;
        }

        public VendorBuilder totalReviews(Integer totalReviews) {
            this.totalReviews = totalReviews;
            return this;
        }

        public Vendor build() {
            return new Vendor(id, user, businessName, serviceType, description, price, city, availabilityStatus, rating, totalReviews);
        }
    }
}
