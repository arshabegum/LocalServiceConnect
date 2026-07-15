package com.LocalServe.LocalServe.controller;

import com.LocalServe.LocalServe.entity.EmergencyOffer;
import com.LocalServe.LocalServe.entity.EmergencyRequest;
import com.LocalServe.LocalServe.entity.User;
import com.LocalServe.LocalServe.entity.Vendor;
import com.LocalServe.LocalServe.service.EmergencyBookingService;
import com.LocalServe.LocalServe.service.VendorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/emergency")
public class EmergencyBookingController {

    @Autowired
    private EmergencyBookingService emergencyService;

    @Autowired
    private VendorService vendorService;

    @PostMapping("/request/create")
    public String createEmergencyRequest(@RequestParam String category,
                                         @RequestParam String bookingDate,
                                         @RequestParam String eventTime,
                                         @RequestParam String address,
                                         @RequestParam(required = false) String notes,
                                         @RequestParam(required = false) Double budget,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"CUSTOMER".equalsIgnoreCase(loggedInUser.getRole())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only customers can make emergency requests!");
            return "redirect:/login";
        }

        try {
            LocalDate date = LocalDate.parse(bookingDate);
            EmergencyRequest req = emergencyService.createRequest(loggedInUser, category, date, eventTime, address, notes, budget);
            redirectAttributes.addFlashAttribute("successMessage", "Your emergency request has been sent to nearby available vendors. Waiting for a vendor to accept your request.");
            return "redirect:/emergency/request/" + req.getId() + "/offers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create request: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/request/{id}/offers")
    public String viewRequestOffers(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"CUSTOMER".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        EmergencyRequest request = emergencyService.getRequestById(id);
        if (request == null || !request.getCustomer().getId().equals(loggedInUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Emergency request not found!");
            return "redirect:/dashboard";
        }

        List<EmergencyOffer> offers = emergencyService.getOffersForRequest(id);

        model.addAttribute("request", request);
        model.addAttribute("offers", offers);
        model.addAttribute("user", loggedInUser);
        return "emergency-offers";
    }

    @PostMapping("/offer/{id}/accept")
    public String acceptOffer(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"CUSTOMER".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        try {
            emergencyService.acceptOffer(id, loggedInUser);
            redirectAttributes.addFlashAttribute("successMessage", "Booking confirmed! You have successfully selected this vendor offer.");
            return "redirect:/bookings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/offer/{id}/reject")
    public String rejectOffer(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"CUSTOMER".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        try {
            emergencyService.rejectOffer(id, loggedInUser);
            redirectAttributes.addFlashAttribute("successMessage", "Vendor offer declined.");
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/requests")
    public String viewCategoryRequests(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"VENDOR".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        Vendor vendor = vendorService.getVendorByUser(loggedInUser);
        if (vendor == null || !"APPROVED".equalsIgnoreCase(vendor.getApprovalStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your vendor profile must be approved to view emergency requests!");
            return "redirect:/dashboard";
        }

        // Get requests in vendor's category
        List<EmergencyRequest> openReqs = new ArrayList<>();
        openReqs.addAll(emergencyService.getPendingRequestsForVendorCategory(vendor.getServiceType()));
        openReqs.addAll(emergencyService.getAcceptedRequestsForVendorCategory(vendor.getServiceType()));

        // Filter out requests already rejected/declined by this vendor
        List<EmergencyOffer> myOffers = emergencyService.getOffersByVendor(vendor);
        List<Long> declinedRequestIds = myOffers.stream()
                .filter(o -> "REJECTED".equalsIgnoreCase(o.getStatus()))
                .map(o -> o.getRequest().getId())
                .collect(Collectors.toList());

        List<EmergencyRequest> filtered = openReqs.stream()
                .filter(r -> !declinedRequestIds.contains(r.getId()))
                .collect(Collectors.toList());

        model.addAttribute("requests", filtered);
        model.addAttribute("vendor", vendor);
        model.addAttribute("user", loggedInUser);
        return "emergency-requests";
    }

    @PostMapping("/offer/create")
    public String createOffer(@RequestParam Long requestId,
                              @RequestParam Double emergencyCharge,
                              @RequestParam String arrivalTime,
                              @RequestParam(required = false) String message,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"VENDOR".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        Vendor vendor = vendorService.getVendorByUser(loggedInUser);
        if (vendor == null || !"APPROVED".equalsIgnoreCase(vendor.getApprovalStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Approved vendors only.");
            return "redirect:/dashboard";
        }

        try {
            emergencyService.createOffer(vendor, requestId, emergencyCharge, arrivalTime, message);
            redirectAttributes.addFlashAttribute("successMessage", "Offer submitted successfully! Waiting for customer feedback.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/emergency/requests";
    }

    @PostMapping("/request/{id}/decline")
    public String declineRequest(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"VENDOR".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        Vendor vendor = vendorService.getVendorByUser(loggedInUser);
        if (vendor == null) {
            return "redirect:/dashboard";
        }

        try {
            emergencyService.declineRequest(vendor, id);
            redirectAttributes.addFlashAttribute("successMessage", "Emergency request declined.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/emergency/requests";
    }
}
