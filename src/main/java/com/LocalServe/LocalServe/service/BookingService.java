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
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private NotificationService notificationService;

    public Booking createBooking(User customer, Long vendorId, LocalDate bookingDate, String notes, String address) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found!"));

        Booking booking = Booking.builder()
                .customer(customer)
                .vendor(vendor)
                .bookingDate(bookingDate)
                .status("PENDING")
                .price(vendor.getPrice())
                .notes(notes)
                .address(address)
                .build();

        Booking saved = bookingRepository.save(booking);
        // Create notifications
        notificationService.createNotification(vendor.getUser(), "🔔 New Booking Request: " + customer.getFullName() + " requested a booking for " + bookingDate + ".");
        return saved;
    }

    public Booking createEmergencyBooking(User customer, String category, String notes, String address) {
        LocalDate today = LocalDate.now();
        List<Vendor> activeVendors = vendorRepository.findAll().stream()
                .filter(v -> v.getServiceType().equalsIgnoreCase(category) && v.getAvailabilityStatus())
                .filter(v -> !isVendorBookedOn(v, today))
                .collect(Collectors.toList());

        if (activeVendors.isEmpty()) {
            throw new RuntimeException("No available vendors in category " + category + " for today's emergency booking!");
        }

        Vendor vendor = activeVendors.get(0);

        Booking booking = Booking.builder()
                .customer(customer)
                .vendor(vendor)
                .bookingDate(today)
                .status("APPROVED") // approved immediately
                .price(vendor.getPrice() * 1.5) // surcharge
                .notes("[EMERGENCY BOOKING] " + (notes != null ? notes : ""))
                .address(address)
                .build();

        Booking saved = bookingRepository.save(booking);
        notificationService.createNotification(customer, "🔔 Emergency Booking Confirmed: Matched with " + vendor.getBusinessName() + " for today!");
        notificationService.createNotification(vendor.getUser(), "🔔 Immediate Emergency Booking: Assigned emergency booking with " + customer.getFullName() + " today!");
        return saved;
    }

    public boolean isVendorBookedOn(Vendor vendor, LocalDate date) {
        return bookingRepository.findByVendorOrderByBookingDateDesc(vendor).stream()
                .anyMatch(b -> b.getBookingDate().equals(date) && 
                        ("APPROVED".equalsIgnoreCase(b.getStatus()) || "COMPLETED".equalsIgnoreCase(b.getStatus()) || "PENDING".equalsIgnoreCase(b.getStatus())));
    }

    public void cancelBooking(Long bookingId, User customer) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found!"));
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Unauthorized cancellation request.");
        }
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        notificationService.createNotification(booking.getVendor().getUser(), "🔔 Booking Cancelled: Customer " + customer.getFullName() + " has cancelled their booking for " + booking.getBookingDate() + ".");
        notificationService.createNotification(customer, "🔔 Cancellation Confirmed: Your booking with " + booking.getVendor().getBusinessName() + " has been cancelled.");
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
        Booking saved = bookingRepository.save(booking);

        if ("APPROVED".equalsIgnoreCase(newStatus)) {
            notificationService.createNotification(booking.getCustomer(), "🔔 Booking Approved: Your booking with " + booking.getVendor().getBusinessName() + " for " + booking.getBookingDate() + " has been approved!");
            notificationService.createNotification(booking.getVendor().getUser(), "🔔 Vendor Accepted: You accepted the booking from " + booking.getCustomer().getFullName() + " for " + booking.getBookingDate() + ".");
        } else if ("REJECTED".equalsIgnoreCase(newStatus)) {
            notificationService.createNotification(booking.getCustomer(), "🔔 Booking Rejected: Your request with " + booking.getVendor().getBusinessName() + " was rejected.");
        } else if ("COMPLETED".equalsIgnoreCase(newStatus)) {
            notificationService.createNotification(booking.getCustomer(), "🔔 Booking Completed: Your event with " + booking.getVendor().getBusinessName() + " is complete! Please rate them.");
        }

        return saved;
    }

    public long getCount() {
        return bookingRepository.count();
    }

    public double getTotalRevenue() {
        return bookingRepository.findAll().stream()
                .filter(b -> "APPROVED".equalsIgnoreCase(b.getStatus()) || "COMPLETED".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(Booking::getPrice)
                .sum();
    }
}
