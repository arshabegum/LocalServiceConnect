package com.LocalServe.LocalServe.repository;

import com.LocalServe.LocalServe.entity.Booking;
import com.LocalServe.LocalServe.entity.User;
import com.LocalServe.LocalServe.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerOrderByBookingDateDesc(User customer);

    List<Booking> findByVendorOrderByBookingDateDesc(Vendor vendor);

    List<Booking> findAllByOrderByBookingDateDesc();
}
