package com.LocalServe.LocalServe.service;

import com.LocalServe.LocalServe.entity.Booking;
import com.LocalServe.LocalServe.entity.User;
import com.LocalServe.LocalServe.entity.Vendor;
import com.LocalServe.LocalServe.repository.BookingRepository;
import com.LocalServe.LocalServe.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private VendorRepository vendorRepository;

    public Booking createBooking(User customer, Long vendorId, LocalDate bookingDate, String notes) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found!"));

        Booking booking = Booking.builder()
                .customer(customer)
                .vendor(vendor)
                .bookingDate(bookingDate)
                .status("PENDING")
                .price(vendor.getPrice())
                .notes(notes)
                .build();

        return bookingRepository.save(booking);
    }

    public List<Booking> getBookingsForCustomer(User customer) {
        return bookingRepository.findByCustomerOrderByBookingDateDesc(customer);
    }

    public List<Booking> getBookingsForVendor(Vendor vendor) {
        return bookingRepository.findByVendorOrderByBookingDateDesc(vendor);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByBookingDateDesc();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    public Booking updateBookingStatus(Long bookingId, String newStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found!"));
        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }

    public long getCount() {
        return bookingRepository.count();
    }
}
