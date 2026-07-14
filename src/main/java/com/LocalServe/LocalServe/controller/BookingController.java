package com.LocalServe.LocalServe.controller;

import com.LocalServe.LocalServe.entity.User;
import com.LocalServe.LocalServe.service.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/create")
    public String createBooking(@RequestParam Long vendorId,
            @RequestParam String bookingDate,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String address,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"CUSTOMER".equalsIgnoreCase(loggedInUser.getRole())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only customers can book services!");
            return "redirect:/login";
        }

        try {
            LocalDate date = LocalDate.parse(bookingDate);
            if (date.isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Booking date cannot be in the past!");
                return "redirect:/search-vendor";
            }

            bookingService.createBooking(loggedInUser, vendorId, date, notes, address);
            redirectAttributes.addFlashAttribute("successMessage", "Booking request sent successfully!");
            return "redirect:/bookings";

        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid date format! Please select a valid date.");
            return "redirect:/search-vendor";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/search-vendor";
        }
    }
}
