package com.LocalServe.LocalServe.service;

import com.LocalServe.LocalServe.entity.*;
import com.LocalServe.LocalServe.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmergencyBookingService {

    @Autowired
    private EmergencyRequestRepository requestRepository;

    @Autowired
    private EmergencyOfferRepository offerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public EmergencyRequest createRequest(User customer, String category, LocalDate date, String time, String location, String notes, Double budget) {
        EmergencyRequest request = new EmergencyRequest(customer, category, date, time, location, notes, budget);
        EmergencyRequest saved = requestRepository.save(request);

        // Notify all APPROVED vendors in that category
        List<Vendor> targetVendors = vendorRepository.findAll().stream()
                .filter(v -> v.getServiceType().equalsIgnoreCase(category) && "APPROVED".equalsIgnoreCase(v.getApprovalStatus()))
                .collect(Collectors.toList());

        for (Vendor v : targetVendors) {
            notificationService.createNotification(v.getUser(), "🔔 New Emergency Request: " + customer.getFullName() + " needs a " + category + " on " + date + " at " + time + ".");
        }

        notificationService.createNotification(customer, "🔔 Request Sent: Your emergency request for " + category + " has been sent to nearby vendors.");
        return saved;
    }

    @Transactional
    public EmergencyOffer createOffer(Vendor vendor, Long requestId, Double emergencyCharge, String arrivalTime, String message) {
        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Emergency request not found!"));

        if ("CONFIRMED".equalsIgnoreCase(request.getStatus())) {
            throw new RuntimeException("This request has already been confirmed with another vendor.");
        }

        // Save or update offer
        Optional<EmergencyOffer> existing = offerRepository.findByRequestAndVendor(request, vendor);
        EmergencyOffer offer;
        if (existing.isPresent()) {
            offer = existing.get();
            offer.setEmergencyCharge(emergencyCharge);
            offer.setArrivalTime(arrivalTime);
            offer.setMessage(message);
            offer.setStatus("PENDING");
        } else {
            offer = new EmergencyOffer(request, vendor, emergencyCharge, arrivalTime, message);
        }

        EmergencyOffer saved = offerRepository.save(offer);

        // If request is pending, mark it accepted (meaning has offers)
        if ("PENDING".equalsIgnoreCase(request.getStatus())) {
            request.setStatus("ACCEPTED");
            requestRepository.save(request);
        }

        notificationService.createNotification(request.getCustomer(), "🔔 Vendor Accepted: " + vendor.getBusinessName() + " has offered emergency service for your request.");
        return saved;
    }

    @Transactional
    public void declineRequest(Vendor vendor, Long requestId) {
        EmergencyRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Emergency request not found!"));

        Optional<EmergencyOffer> existing = offerRepository.findByRequestAndVendor(request, vendor);
        EmergencyOffer offer;
        if (existing.isPresent()) {
            offer = existing.get();
        } else {
            offer = new EmergencyOffer(request, vendor, 0.0, "N/A", "Declined");
        }
        offer.setStatus("REJECTED");
        offerRepository.save(offer);
    }

    @Transactional
    public Booking acceptOffer(Long offerId, User customer) {
        EmergencyOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found!"));

        EmergencyRequest request = offer.getRequest();
        if (!request.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Unauthorized offer acceptance.");
        }

        if ("CONFIRMED".equalsIgnoreCase(request.getStatus())) {
            throw new RuntimeException("This request is already confirmed.");
        }

        // Update statuses
        offer.setStatus("ACCEPTED");
        offerRepository.save(offer);

        request.setStatus("CONFIRMED");
        requestRepository.save(request);

        // Reject/close other offers
        List<EmergencyOffer> allOffers = offerRepository.findByRequest(request);
        for (EmergencyOffer o : allOffers) {
            if (!o.getId().equals(offer.getId())) {
                o.setStatus("CLOSED");
                offerRepository.save(o);
                notificationService.createNotification(o.getVendor().getUser(), "🔔 Customer Declined Your Offer: Emergency request for " + customer.getFullName() + " was assigned to another vendor.");
            }
        }

        // Create standard confirmed booking
        Vendor vendor = offer.getVendor();
        Booking booking = Booking.builder()
                .customer(customer)
                .vendor(vendor)
                .bookingDate(request.getEventDate())
                .status("APPROVED") // confirmed immediately
                .price(vendor.getPrice() + offer.getEmergencyCharge())
                .notes("[EMERGENCY BOOKING] Time: " + request.getEventTime() + ". Details: " + request.getNotes() + ". Offer note: " + offer.getMessage())
                .address(request.getEventLocation())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        notificationService.createNotification(customer, "🔔 Booking Confirmed: Your emergency booking with " + vendor.getBusinessName() + " has been confirmed.");
        notificationService.createNotification(vendor.getUser(), "🔔 Customer Accepted Your Offer: You are booked for an emergency event on " + request.getEventDate() + " at " + request.getEventTime() + ".");

        return savedBooking;
    }

    @Transactional
    public void rejectOffer(Long offerId, User customer) {
        EmergencyOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found!"));

        if (!offer.getRequest().getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Unauthorized offer rejection.");
        }

        offer.setStatus("REJECTED");
        offerRepository.save(offer);

        notificationService.createNotification(offer.getVendor().getUser(), "🔔 Customer Declined Your Offer: Your emergency offer has been declined.");
    }

    public List<EmergencyRequest> getRequestsForCustomer(User customer) {
        return requestRepository.findByCustomerOrderByIdDesc(customer);
    }

    public List<EmergencyRequest> getPendingRequestsForVendorCategory(String category) {
        // Pending or Accepted status requests are open for bidding
        return requestRepository.findByServiceCategoryAndStatusOrderByIdDesc(category, "PENDING").stream()
                .collect(Collectors.toList());
    }

    public List<EmergencyRequest> getAcceptedRequestsForVendorCategory(String category) {
        return requestRepository.findByServiceCategoryAndStatusOrderByIdDesc(category, "ACCEPTED").stream()
                .collect(Collectors.toList());
    }

    public List<EmergencyOffer> getOffersForRequest(Long requestId) {
        EmergencyRequest request = requestRepository.findById(requestId).orElse(null);
        if (request == null) return List.of();
        return offerRepository.findByRequest(request).stream()
                .filter(o -> !"CLOSED".equalsIgnoreCase(o.getStatus()) && !"REJECTED".equalsIgnoreCase(o.getStatus()))
                .collect(Collectors.toList());
    }

    public EmergencyRequest getRequestById(Long id) {
        return requestRepository.findById(id).orElse(null);
    }

    public List<EmergencyOffer> getOffersByVendor(Vendor vendor) {
        return offerRepository.findByVendor(vendor);
    }
}
