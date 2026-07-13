package com.LocalServe.LocalServe.controller;

import com.LocalServe.LocalServe.entity.*;
import com.LocalServe.LocalServe.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PageController {

    @Autowired
    private VendorService vendorService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private ContactMessageService contactMessageService;

    @GetMapping("/")
    public String home(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }
        return "index";
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String register(HttpSession session) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loggedInUser);
        String role = loggedInUser.getRole().toUpperCase();

        if ("CUSTOMER".equals(role)) {
            List<Booking> customerBookings = bookingService.getBookingsForCustomer(loggedInUser);
            model.addAttribute("totalBookings", customerBookings.size());
            
            List<Review> customerReviews = reviewService.getReviewsByCustomer(loggedInUser);
            model.addAttribute("totalReviews", customerReviews.size());
            
            List<Vendor> activeVendors = vendorService.getAllVendors();
            model.addAttribute("activeVendorsCount", activeVendors.size());

            // Limit to top 5 recent bookings
            List<Booking> recentBookings = customerBookings.subList(0, Math.min(5, customerBookings.size()));
            model.addAttribute("recentBookings", recentBookings);

        } else if ("VENDOR".equals(role)) {
            Vendor vendor = vendorService.getVendorByUser(loggedInUser);
            model.addAttribute("vendor", vendor);
            
            if (vendor != null) {
                List<Booking> vendorBookings = bookingService.getBookingsForVendor(vendor);
                model.addAttribute("totalBookings", vendorBookings.size());
                
                List<Review> vendorReviews = reviewService.getReviewsForVendor(vendor);
                model.addAttribute("totalReviews", vendorReviews.size());
                model.addAttribute("avgRating", vendor.getRating());

                List<Booking> recentBookings = vendorBookings.subList(0, Math.min(5, vendorBookings.size()));
                model.addAttribute("recentBookings", recentBookings);
            }

        } else if ("ADMIN".equals(role)) {
            List<Booking> allBookings = bookingService.getAllBookings();
            model.addAttribute("totalBookings", allBookings.size());
            
            List<Vendor> allVendors = vendorService.getAllVendors();
            model.addAttribute("totalVendors", allVendors.size());
            
            List<Review> allReviews = reviewService.getAllReviews();
            model.addAttribute("totalReviews", allReviews.size());
            
            // Add user list stats
            model.addAttribute("totalUsers", userService.getAllUsersCount());

            List<Booking> recentBookings = allBookings.subList(0, Math.min(5, allBookings.size()));
            model.addAttribute("recentBookings", recentBookings);
        }

        return "dashboard";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loggedInUser);
        
        if ("VENDOR".equalsIgnoreCase(loggedInUser.getRole())) {
            Vendor vendor = vendorService.getVendorByUser(loggedInUser);
            model.addAttribute("vendor", vendor);
        }

        return "profile";
    }

    @GetMapping("/search-vendor")
    public String searchVendor(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name,
            HttpSession session,
            Model model) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        
        List<Vendor> vendors = vendorService.searchVendors(category, city, name);
        model.addAttribute("vendors", vendors);
        
        // Pass filter criteria back to display in fields
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedName", name);
        model.addAttribute("user", loggedInUser);

        return "search-vendor";
    }

    @GetMapping("/bookings")
    public String bookings(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loggedInUser);
        String role = loggedInUser.getRole().toUpperCase();

        if ("CUSTOMER".equals(role)) {
            model.addAttribute("bookings", bookingService.getBookingsForCustomer(loggedInUser));
        } else if ("VENDOR".equals(role)) {
            Vendor vendor = vendorService.getVendorByUser(loggedInUser);
            model.addAttribute("bookings", bookingService.getBookingsForVendor(vendor));
        } else if ("ADMIN".equals(role)) {
            model.addAttribute("bookings", bookingService.getAllBookings());
        }

        return "bookings";
    }

    @GetMapping("/reviews")
    public String reviews(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loggedInUser);
        String role = loggedInUser.getRole().toUpperCase();

        if ("CUSTOMER".equals(role)) {
            model.addAttribute("reviews", reviewService.getReviewsByCustomer(loggedInUser));
            // Let the customer select which vendor they'd like to write a review for
            model.addAttribute("vendors", vendorService.getAllVendors());
        } else if ("VENDOR".equals(role)) {
            Vendor vendor = vendorService.getVendorByUser(loggedInUser);
            model.addAttribute("reviews", reviewService.getReviewsForVendor(vendor));
        } else if ("ADMIN".equals(role)) {
            model.addAttribute("reviews", reviewService.getAllReviews());
        }

        return "reviews";
    }

    @GetMapping("/contact")
    public String contact(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        // Contact is accessible to anyone (even logged out)
        if (loggedInUser != null) {
            model.addAttribute("user", loggedInUser);
            if ("ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
                model.addAttribute("messages", contactMessageService.getAllMessages());
            }
        }
        return "contact";
    }
}