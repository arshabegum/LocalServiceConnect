package com.LocalServe.LocalServe.controller;

import com.LocalServe.LocalServe.service.ContactMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/contact")
public class ContactController {

    @Autowired
    private ContactMessageService contactMessageService;

    @PostMapping("/send")
    public String sendMessage(@RequestParam String name,
            @RequestParam String email,
            @RequestParam String message,
            @RequestParam(required = false) String recipientEmail,
            RedirectAttributes redirectAttributes) {

        if (name.trim().isEmpty() || email.trim().isEmpty() || message.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "All fields are required!");
            return "redirect:/contact";
        }

        try {
            // If admin is replying, recipientEmail will be provided — save the outgoing
            // message with recipient's email
            String targetEmail = (recipientEmail != null && !recipientEmail.trim().isEmpty()) ? recipientEmail.trim()
                    : email.trim();
            contactMessageService.saveMessage(name, targetEmail, message);
            redirectAttributes.addFlashAttribute("successMessage", "Your message has been sent. Thank you!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send message. Try again later.");
        }

        return "redirect:/contact";
    }
}
