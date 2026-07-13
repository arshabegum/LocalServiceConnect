package com.LocalServe.LocalServe.repository;

import com.LocalServe.LocalServe.entity.Vendor;
import com.LocalServe.LocalServe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByUser(User user);

    @Query("SELECT v FROM Vendor v WHERE " +
           "(:category IS NULL OR v.serviceType = :category) AND " +
           "(:city IS NULL OR :city = '' OR LOWER(v.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:name IS NULL OR :name = '' OR LOWER(v.businessName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(v.user.fullName) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<Vendor> searchVendors(@Param("category") String category, 
                               @Param("city") String city, 
                               @Param("name") String name);
}
