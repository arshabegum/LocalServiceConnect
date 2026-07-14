package com.LocalServe.LocalServe.controller;

import com.LocalServe.LocalServe.entity.*;
import com.LocalServe.LocalServe.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private NotificationService notificationService;

    public static class DateAvailability {
        private String dateStr;
        private boolean available;

        public DateAvailability(String dateStr, boolean available) {
            this.dateStr = dateStr;
            this.available = available;
        }

        public String getDateStr() {
            return dateStr;
        }

        public boolean isAvailable() {
            return available;
        }
    }

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

        // Get notifications
        List<Notification> notifications = notificationService.getNotificationsForUser(loggedInUser);
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", notifications.stream().filter(n -> !n.getIsRead()).count());

        if ("CUSTOMER".equals(role)) {
            List<Booking> customerBookings = bookingService.getBookingsForCustomer(loggedInUser);
            model.addAttribute("totalBookings", customerBookings.size());

            List<Review> customerReviews = reviewService.getReviewsByCustomer(loggedInUser);
            model.addAttribute("totalReviews", customerReviews.size());

            List<Vendor> activeVendors = vendorService.getAllVendors();
            model.addAttribute("activeVendorsCount", activeVendors.size());

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
            model.addAttribute("pendingVendors", allVendors.stream().filter(v -> "PENDING".equalsIgnoreCase(v.getApprovalStatus())).count());
            model.addAttribute("approvedVendors", allVendors.stream().filter(v -> "APPROVED".equalsIgnoreCase(v.getApprovalStatus())).count());
            model.addAttribute("rejectedVendors", allVendors.stream().filter(v -> "REJECTED".equalsIgnoreCase(v.getApprovalStatus())).count());

            model.addAttribute("totalCustomers", userService.getCustomersCount());
            model.addAttribute("totalUsers", userService.getAllUsersCount());
            
            // Calculate actual database revenue
            model.addAttribute("totalRevenue", bookingService.getTotalRevenue());

            List<Booking> recentBookings = allBookings.subList(0, Math.min(5, allBookings.size()));
            model.addAttribute("recentBookings", recentBookings);
        }

        // Mark notifications as read upon viewing dashboard
        notificationService.markAllAsRead(loggedInUser);

        return "dashboard";
    }

    @GetMapping("/admin/vendors")
    public String adminVendors(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            HttpSession session,
            Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        List<Vendor> all = vendorService.getAllVendors();

        // Apply filters
        List<Vendor> filtered = all.stream()
                .filter(v -> {
                    if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
                        return status.equalsIgnoreCase(v.getApprovalStatus());
                    }
                    return true;
                })
                .filter(v -> {
                    if (search != null && !search.trim().isEmpty()) {
                        String s = search.toLowerCase();
                        return v.getBusinessName().toLowerCase().contains(s) || 
                               v.getUser().getFullName().toLowerCase().contains(s) ||
                               v.getUser().getEmail().toLowerCase().contains(s);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        model.addAttribute("vendors", filtered);
        model.addAttribute("selectedStatus", status != null ? status : "ALL");
        model.addAttribute("searchQuery", search);
        model.addAttribute("user", loggedInUser);

        // Add counters for dashboard summary
        model.addAttribute("totalVendors", all.size());
        model.addAttribute("pendingVendors", all.stream().filter(v -> "PENDING".equalsIgnoreCase(v.getApprovalStatus())).count());
        model.addAttribute("approvedVendors", all.stream().filter(v -> "APPROVED".equalsIgnoreCase(v.getApprovalStatus())).count());
        model.addAttribute("rejectedVendors", all.stream().filter(v -> "REJECTED".equalsIgnoreCase(v.getApprovalStatus())).count());
        model.addAttribute("totalCustomers", userService.getCustomersCount());
        model.addAttribute("totalBookings", bookingService.getAllBookings().size());

        return "admin-vendors";
    }

    @GetMapping("/vendors/availability")
    public String manageAvailability(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"VENDOR".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/login";
        }

        Vendor vendor = vendorService.getVendorByUser(loggedInUser);
        model.addAttribute("vendor", vendor);
        model.addAttribute("user", loggedInUser);

        return "manage-availability";
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

        // Generate availability for next 5 days for each vendor
        Map<Long, List<DateAvailability>> availabilityMap = new HashMap<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for (Vendor v : vendors) {
            List<DateAvailability> availabilities = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                LocalDate date = today.plusDays(i);
                boolean isBooked = bookingService.isVendorBookedOn(v, date);
                availabilities.add(new DateAvailability(date.format(formatter), !isBooked));
            }
            availabilityMap.put(v.getId(), availabilities);
        }

        model.addAttribute("availabilityMap", availabilityMap);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedName", name);
        model.addAttribute("user", loggedInUser);

        return "search-vendor";
    }

    @GetMapping("/budget-calculator")
    public String budgetCalculator(
            @RequestParam(required = false) Double budget,
            HttpSession session,
            Model model) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", loggedInUser);
        model.addAttribute("budget", budget);

        if (budget != null && budget > 0) {
            // Find all active vendors
            List<Vendor> allVendors = vendorService.getAllVendors();
            
            // Group vendors by category
            Map<String, List<Vendor>> grouped = allVendors.stream()
                    .filter(Vendor::getAvailabilityStatus)
                    .collect(Collectors.groupingBy(Vendor::getServiceType));

            List<Vendor> suggestedVendors = new ArrayList<>();
            double totalCost = 0;

            // Simple recommendation algorithm: Pick the cheapest/highest-rated vendor per category that fits the budget.
            // Sort categories to process systematically
            String[] categories = {"Photographer", "Caterer", "Decorator", "DJ", "Makeup Artist"};
            for (String cat : categories) {
                List<Vendor> catVendors = grouped.get(cat);
                if (catVendors != null && !catVendors.isEmpty()) {
                    // Sort by price ascending, rating descending
                    catVendors.sort(Comparator.comparing(Vendor::getPrice)
                            .thenComparing(Comparator.comparing(Vendor::getRating).reversed()));
                    
                    Vendor chosen = catVendors.get(0); // Choose cheapest
                    suggestedVendors.add(chosen);
                    totalCost += chosen.getPrice();
                }
            }

            model.addAttribute("suggestedVendors", suggestedVendors);
            model.addAttribute("totalCost", totalCost);
            model.addAttribute("isWithinBudget", totalCost <= budget);
        }

        return "budget-calculator";
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
        if (loggedInUser != null) {
            model.addAttribute("user", loggedInUser);
            String role = loggedInUser.getRole().toUpperCase();
            if ("ADMIN".equals(role)) {
                List<ContactMessage> all = contactMessageService.getAllMessages();
                List<ContactMessage> incoming = all.stream()
                        .filter(m -> !m.getName().equalsIgnoreCase(loggedInUser.getFullName()))
                        .collect(Collectors.toList());
                model.addAttribute("messages", incoming);
            } else if ("CUSTOMER".equals(role)) {
                model.addAttribute("messages", contactMessageService.getMessagesForEmail(loggedInUser.getEmail()));
            }
        }
        return "contact";
    }
}