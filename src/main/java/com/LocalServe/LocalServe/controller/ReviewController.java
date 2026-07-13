package com.LocalServe.LocalServe.controller;

import com.LocalServe.LocalServe.entity.User;
import com.LocalServe.LocalServe.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/submit")
    public String submitReview(@RequestParam Long vendorId,
                               @RequestParam Integer rating,
                               @RequestParam String comment,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"CUSTOMER".equalsIgnoreCase(loggedInUser.getRole())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only customers can write reviews!");
            return "redirect:/login";
        }

        if (rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute("errorMessage", "Rating must be between 1 and 5!");
            return "redirect:/reviews";
        }

        try {
            reviewService.addReview(loggedInUser, vendorId, rating, comment);
            redirectAttributes.addFlashAttribute("successMessage", "Review submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit review: " + e.getMessage());
        }

        return "redirect:/reviews";
    }
}
