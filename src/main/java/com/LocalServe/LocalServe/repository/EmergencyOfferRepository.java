package com.LocalServe.LocalServe.repository;

import com.LocalServe.LocalServe.entity.EmergencyOffer;
import com.LocalServe.LocalServe.entity.EmergencyRequest;
import com.LocalServe.LocalServe.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyOfferRepository extends JpaRepository<EmergencyOffer, Long> {
    List<EmergencyOffer> findByRequest(EmergencyRequest request);
    List<EmergencyOffer> findByVendor(Vendor vendor);
    Optional<EmergencyOffer> findByRequestAndVendor(EmergencyRequest request, Vendor vendor);
}
