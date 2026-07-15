package com.LocalServe.LocalServe.controller;

import com.LocalServe.LocalServe.entity.Booking;
import com.LocalServe.LocalServe.entity.User;
import com.LocalServe.LocalServe.service.BookingService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;

import java.awt.Color;
import java.io.IOException;
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

    @PostMapping("/cancel")
    public String cancelBooking(@RequestParam Long bookingId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"CUSTOMER".equalsIgnoreCase(loggedInUser.getRole())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only customers can cancel bookings!");
            return "redirect:/login";
        }

        try {
            bookingService.cancelBooking(bookingId, loggedInUser);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/bookings";
    }

    @GetMapping("/{id}/receipt")
    public void downloadReceipt(@PathVariable Long id, HttpSession session, HttpServletResponse response)
            throws IOException {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            response.sendRedirect("/login");
            return;
        }

        Booking booking = bookingService.getBookingById(id);
        if (booking == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found");
            return;
        }

        // Security check: Only customer who booked, vendor assigned, or admin can
        // download receipt
        boolean authorized = loggedInUser.getRole().equalsIgnoreCase("ADMIN") ||
                booking.getCustomer().getId().equals(loggedInUser.getId()) ||
                (booking.getVendor() != null && booking.getVendor().getUser().getId().equals(loggedInUser.getId()));

        if (!authorized) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized to access this receipt");
            return;
        }

        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=booking_receipt_" + id + ".pdf";
        response.setHeader(headerKey, headerValue);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.DARK_GRAY);
        Font fontSubtitle = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY);
        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
        Font fontBody = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

        Paragraph title = new Paragraph("Local Service Connect", fontTitle);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("Booking Invoice & Receipt", fontSubtitle);
        subtitle.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(subtitle);

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Booking ID: LSC-2026-" + booking.getId(), fontBody));
        document.add(new Paragraph("Issue Date: " + LocalDate.now(), fontBody));
        document.add(new Paragraph("Status: " + booking.getStatus(), fontBody));
        document.add(new Paragraph(
                "----------------------------------------------------------------------------------------------------------------------------------"));

        document.add(new Paragraph("\nCUSTOMER DETAILS",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY)));
        document.add(new Paragraph("Name: " + booking.getCustomer().getFullName(), fontBody));
        document.add(new Paragraph("Email: " + booking.getCustomer().getEmail(), fontBody));
        document.add(new Paragraph("Phone: " + booking.getCustomer().getPhoneNumber(), fontBody));

        document.add(new Paragraph("\nVENDOR DETAILS",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY)));
        document.add(new Paragraph("Business Name: " + booking.getVendor().getBusinessName(), fontBody));
        document.add(new Paragraph("Category: " + booking.getVendor().getServiceType(), fontBody));
        document.add(new Paragraph("Vendor Phone: " + booking.getVendor().getUser().getPhoneNumber(), fontBody));

        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(new float[] { 1f, 2f });
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        PdfPCell headerCell1 = new PdfPCell(new Paragraph("Description", fontHeader));
        headerCell1.setBackgroundColor(new Color(30, 58, 95));
        headerCell1.setPadding(8);
        headerCell1.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerCell1.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell headerCell2 = new PdfPCell(new Paragraph("Details", fontHeader));
        headerCell2.setBackgroundColor(new Color(30, 58, 95));
        headerCell2.setPadding(8);
        headerCell2.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerCell2.setVerticalAlignment(Element.ALIGN_MIDDLE);

        table.addCell(headerCell1);
        table.addCell(headerCell2);

        PdfPCell labelCell;
        PdfPCell valueCell;

        labelCell = new PdfPCell(new Paragraph("Booking Date", fontBody));
        labelCell.setPadding(6);
        labelCell.setBorderColor(Color.LIGHT_GRAY);
        labelCell.setBackgroundColor(Color.WHITE);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setVerticalAlignment(Element.ALIGN_TOP);
        table.addCell(labelCell);

        valueCell = new PdfPCell(new Paragraph(booking.getBookingDate().toString(), fontBody));
        valueCell.setPadding(6);
        valueCell.setBorderColor(Color.LIGHT_GRAY);
        valueCell.setBackgroundColor(Color.WHITE);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        valueCell.setVerticalAlignment(Element.ALIGN_TOP);
        valueCell.setNoWrap(false);
        table.addCell(valueCell);

        labelCell = new PdfPCell(new Paragraph("Service Location", fontBody));
        labelCell.setPadding(6);
        labelCell.setBorderColor(Color.LIGHT_GRAY);
        labelCell.setBackgroundColor(Color.WHITE);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setVerticalAlignment(Element.ALIGN_TOP);
        table.addCell(labelCell);

        valueCell = new PdfPCell(new Paragraph(booking.getAddress() != null ? booking.getAddress() : "N/A", fontBody));
        valueCell.setPadding(6);
        valueCell.setBorderColor(Color.LIGHT_GRAY);
        valueCell.setBackgroundColor(Color.WHITE);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        valueCell.setVerticalAlignment(Element.ALIGN_TOP);
        valueCell.setNoWrap(false);
        table.addCell(valueCell);

        labelCell = new PdfPCell(new Paragraph("Special Instructions", fontBody));
        labelCell.setPadding(6);
        labelCell.setBorderColor(Color.LIGHT_GRAY);
        labelCell.setBackgroundColor(Color.WHITE);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setVerticalAlignment(Element.ALIGN_TOP);
        table.addCell(labelCell);

        String cleanedNotes = booking.getNotes();
        if (cleanedNotes != null) {
            cleanedNotes = cleanedNotes.replace(". Details: . Offer note:", "").trim();
            if (cleanedNotes.isEmpty()) {
                cleanedNotes = "N/A";
            }
        } else {
            cleanedNotes = "N/A";
        }

        valueCell = new PdfPCell(new Paragraph(cleanedNotes, fontBody));
        valueCell.setPadding(6);
        valueCell.setBorderColor(Color.LIGHT_GRAY);
        valueCell.setBackgroundColor(Color.WHITE);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        valueCell.setVerticalAlignment(Element.ALIGN_TOP);
        valueCell.setNoWrap(false);
        table.addCell(valueCell);

        labelCell = new PdfPCell(new Paragraph("Total Price", fontBody));
        labelCell.setPadding(6);
        labelCell.setBorderColor(Color.LIGHT_GRAY);
        labelCell.setBackgroundColor(Color.WHITE);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setVerticalAlignment(Element.ALIGN_TOP);
        table.addCell(labelCell);

        valueCell = new PdfPCell(new Paragraph("INR " + booking.getPrice(), fontBody));
        valueCell.setPadding(6);
        valueCell.setBorderColor(Color.LIGHT_GRAY);
        valueCell.setBackgroundColor(Color.WHITE);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        valueCell.setVerticalAlignment(Element.ALIGN_TOP);
        valueCell.setNoWrap(false);
        table.addCell(valueCell);

        document.add(table);

        document.add(new Paragraph("\n\nThank you for choosing Local Service Connect!", fontSubtitle));

        document.close();
    }
}
