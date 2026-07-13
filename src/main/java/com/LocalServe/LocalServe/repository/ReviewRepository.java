package com.LocalServe.LocalServe.repository;

import com.LocalServe.LocalServe.entity.Review;
import com.LocalServe.LocalServe.entity.User;
import com.LocalServe.LocalServe.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByVendorOrderByReviewDateDesc(Vendor vendor);

    List<Review> findByCustomerOrderByReviewDateDesc(User customer);
}
