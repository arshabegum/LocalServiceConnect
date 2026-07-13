package com.LocalServe.LocalServe.controller;

import com.LocalServe.LocalServe.entity.User;
import com.LocalServe.LocalServe.entity.Vendor;
import com.LocalServe.LocalServe.service.UserService;
import com.LocalServe.LocalServe.service.VendorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private VendorService vendorService;

    @PostMapping("/users/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            User registeredUser = userService.registerUser(user);
            
            // If registered user is a Vendor, auto-create their vendor profile
            if ("VENDOR".equalsIgnoreCase(registeredUser.getRole())) {
                vendorService.createDefaultVendorProfile(registeredUser);
            }
            
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User user = userService.loginUser(email, password);
        
        if (user != null) {
            session.setAttribute("loggedInUser", user);
            
            if ("VENDOR".equalsIgnoreCase(user.getRole())) {
                Vendor vendor = vendorService.getVendorByUser(user);
                session.setAttribute("loggedInVendor", vendor);
            }
            
            return "redirect:/dashboard";
        }
        
        redirectAttributes.addFlashAttribute("errorMessage", "Invalid email or password!");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }

    @PostMapping("/users/profile/update")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam String phoneNumber,
                                @RequestParam(required = false) String password,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        try {
            User updatedUser = userService.updateUserProfile(loggedInUser.getId(), fullName, phoneNumber, password);
            session.setAttribute("loggedInUser", updatedUser); // Refresh user in session
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile: " + e.getMessage());
        }

        return "redirect:/profile";
    }
}