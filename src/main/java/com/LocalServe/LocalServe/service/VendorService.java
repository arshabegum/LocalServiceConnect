package com.LocalServe.LocalServe.service;

import com.LocalServe.LocalServe.entity.User;
import com.LocalServe.LocalServe.entity.Vendor;
import com.LocalServe.LocalServe.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    public Vendor getVendorByUser(User user) {
        return vendorRepository.findByUser(user).orElse(null);
    }

    public Vendor getVendorById(Long id) {
        return vendorRepository.findById(id).orElse(null);
    }

    public Vendor saveVendor(Vendor vendor) {
        return vendorRepository.save(vendor);
    }

    public List<Vendor> searchVendors(String category, String city, String name) {
        // Map "All Categories" or empty category to null for the query
        String categoryParam = (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("All Categories")) ? null : category;
        String cityParam = (city == null || city.trim().isEmpty()) ? null : city;
        String nameParam = (name == null || name.trim().isEmpty()) ? null : name;

        return vendorRepository.searchVendors(categoryParam, cityParam, nameParam);
    }

    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    public void createDefaultVendorProfile(User user) {
        if (vendorRepository.findByUser(user).isEmpty()) {
            Vendor vendor = Vendor.builder()
                    .user(user)
                    .businessName(user.getFullName() + " Services")
                    .serviceType("Photographer") // Default category
                    .description("Welcome to my profile. Set up your services details here.")
                    .price(0.0)
                    .city("Change City")
                    .availabilityStatus(true)
                    .rating(0.0)
                    .totalReviews(0)
                    .build();
            vendorRepository.save(vendor);
        }
    }
}
