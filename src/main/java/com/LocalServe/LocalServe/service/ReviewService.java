package com.LocalServe.LocalServe.service;

import com.LocalServe.LocalServe.entity.Review;
import com.LocalServe.LocalServe.entity.User;
import com.LocalServe.LocalServe.entity.Vendor;
import com.LocalServe.LocalServe.repository.ReviewRepository;
import com.LocalServe.LocalServe.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Transactional
    public Review addReview(User customer, Long vendorId, Integer rating, String comment) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found!"));

        Review review = Review.builder()
                .customer(customer)
                .vendor(vendor)
                .rating(rating)
                .comment(comment)
                .reviewDate(LocalDate.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Recalculate vendor average rating
        List<Review> vendorReviews = reviewRepository.findByVendorOrderByReviewDateDesc(vendor);
        double totalRatingSum = 0;
        for (Review r : vendorReviews) {
            totalRatingSum += r.getRating();
        }
        
        double avgRating = totalRatingSum / vendorReviews.size();
        // Round to 1 decimal place
        avgRating = Math.round(avgRating * 10.0) / 10.0;

        vendor.setRating(avgRating);
        vendor.setTotalReviews(vendorReviews.size());
        vendorRepository.save(vendor);

        return savedReview;
    }

    public List<Review> getReviewsForVendor(Vendor vendor) {
        return reviewRepository.findByVendorOrderByReviewDateDesc(vendor);
    }

    public List<Review> getReviewsByCustomer(User customer) {
        return reviewRepository.findByCustomerOrderByReviewDateDesc(customer);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }
}
