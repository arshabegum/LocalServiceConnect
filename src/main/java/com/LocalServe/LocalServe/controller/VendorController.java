package com.LocalServe.LocalServe.controller;
import com.LocalServe.LocalServe.entity.Booking;
import com.LocalServe.LocalServe.entity.User;
import com.LocalServe.LocalServe.entity.Vendor;
import com.LocalServe.LocalServe.service.BookingService;
import com.LocalServe.LocalServe.service.VendorService;
import com.LocalServe.LocalServe.service.VendorAvailabilityService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vendors")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VendorAvailabilityService availabilityService;

    @Autowired
    private com.LocalServe.LocalServe.service.NotificationService notificationService;

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String businessName,
                                @RequestParam String serviceType,
                                @RequestParam String description,
                                @RequestParam Double price,
                                @RequestParam String city,
                                @RequestParam(required = false) Boolean availabilityStatus,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"VENDOR".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        Vendor vendor = vendorService.getVendorByUser(loggedInUser);
        if (vendor == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor profile not found!");
            return "redirect:/profile";
        }

        vendor.setBusinessName(businessName);
        vendor.setServiceType(serviceType);
        vendor.setDescription(description);
        vendor.setPrice(price);
        vendor.setCity(city);
        vendor.setAvailabilityStatus(availabilityStatus != null && availabilityStatus);

        vendorService.saveVendor(vendor);
        session.setAttribute("loggedInVendor", vendor); // refresh session cache

        redirectAttributes.addFlashAttribute("successMessage", "Vendor details updated successfully!");
        return "redirect:/profile";
    }

    @PostMapping("/bookings/update-status")
    public String updateBookingStatus(@RequestParam Long bookingId,
                                      @RequestParam String status,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found!");
            return "redirect:/bookings";
        }

        // Verify authorization: logged in user must be the vendor of this booking OR an admin
        boolean isAuthorized = false;
        if ("ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
            isAuthorized = true;
        } else if ("VENDOR".equalsIgnoreCase(loggedInUser.getRole())) {
            Vendor vendor = vendorService.getVendorByUser(loggedInUser);
            if (vendor != null && booking.getVendor().getId().equals(vendor.getId())) {
                isAuthorized = true;
            }
        }

        if (!isAuthorized) {
            redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to modify this booking!");
            return "redirect:/bookings";
        }

        bookingService.updateBookingStatus(bookingId, status.toUpperCase());
        redirectAttributes.addFlashAttribute("successMessage", "Booking status updated to " + status + "!");
        return "redirect:/bookings";
    }

    @PostMapping("/admin/approve")
    public String approveVendor(@RequestParam Long vendorId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        Vendor vendor = vendorService.getVendorById(vendorId);
        if (vendor == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor not found!");
            return "redirect:/dashboard";
        }

        vendor.setApprovalStatus("APPROVED");
        vendorService.saveVendor(vendor);

        notificationService.createNotification(vendor.getUser(), "🟢 Your account has been approved by the Admin! You can now receive bookings.");
        redirectAttributes.addFlashAttribute("successMessage", "Vendor " + vendor.getBusinessName() + " approved successfully!");
        return "redirect:/admin/vendors";
    }

    @PostMapping("/admin/reject")
    public String rejectVendor(@RequestParam Long vendorId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        Vendor vendor = vendorService.getVendorById(vendorId);
        if (vendor == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor not found!");
            return "redirect:/dashboard";
        }

        vendor.setApprovalStatus("REJECTED");
        vendorService.saveVendor(vendor);

        notificationService.createNotification(vendor.getUser(), "🔴 Your account has been rejected by the Admin. Please edit profile to request re-approval.");
        redirectAttributes.addFlashAttribute("successMessage", "Vendor " + vendor.getBusinessName() + " rejected.");
        return "redirect:/admin/vendors";
    }

    @PostMapping("/admin/reapply")
    public String requestReapproval(HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"VENDOR".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        Vendor vendor = vendorService.getVendorByUser(loggedInUser);
        if (vendor == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor profile not found!");
            return "redirect:/profile";
        }

        vendor.setApprovalStatus("PENDING");
        vendorService.saveVendor(vendor);
        session.setAttribute("loggedInVendor", vendor); // refresh cache

        redirectAttributes.addFlashAttribute("successMessage", "Re-applied for admin approval successfully!");
        return "redirect:/dashboard";
    }

    @PostMapping("/availability/update")
    public String updateAvailability(@RequestParam String date,
                                     @RequestParam String status,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"VENDOR".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        Vendor vendor = vendorService.getVendorByUser(loggedInUser);
        if (vendor == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vendor profile not found!");
            return "redirect:/vendors/availability";
        }

        try {
            java.time.LocalDate localDate = java.time.LocalDate.parse(date);
            availabilityService.setAvailability(vendor, localDate, status);
            redirectAttributes.addFlashAttribute("successMessage", "Availability for " + date + " set to " + status + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        return "redirect:/vendors/availability";
    }
}
