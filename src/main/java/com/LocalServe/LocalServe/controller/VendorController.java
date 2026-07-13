package com.LocalServe.LocalServe.controller;
import com.LocalServe.LocalServe.entity.Booking;
import com.LocalServe.LocalServe.entity.User;
import com.LocalServe.LocalServe.entity.Vendor;
import com.LocalServe.LocalServe.service.BookingService;
import com.LocalServe.LocalServe.service.VendorService;
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
}
