package com.LocalServe.LocalServe.repository;

import com.LocalServe.LocalServe.entity.EmergencyRequest;
import com.LocalServe.LocalServe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, Long> {
    List<EmergencyRequest> findByCustomerOrderByIdDesc(User customer);
    List<EmergencyRequest> findByServiceCategoryAndStatusOrderByIdDesc(String category, String status);
}
