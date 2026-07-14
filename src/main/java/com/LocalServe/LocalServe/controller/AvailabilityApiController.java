package com.LocalServe.LocalServe.controller;

import com.LocalServe.LocalServe.entity.Vendor;
import com.LocalServe.LocalServe.service.VendorAvailabilityService;
import com.LocalServe.LocalServe.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vendors")
public class AvailabilityApiController {

    @Autowired
    private VendorService vendorService;

    @Autowired
    private VendorAvailabilityService availabilityService;

    @GetMapping("/{id}/availability")
    public Map<String, String> getMonthlyAvailability(@PathVariable Long id,
                                                      @RequestParam int year,
                                                      @RequestParam int month) {
        Map<String, String> result = new HashMap<>();
        Vendor vendor = vendorService.getVendorById(id);
        if (vendor == null) {
            return result;
        }

        YearMonth ym = YearMonth.of(year, month);
        int length = ym.lengthOfMonth();

        for (int day = 1; day <= length; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            String status = availabilityService.resolveDateStatus(vendor, date);
            result.put(date.toString(), status);
        }

        return result;
    }
}
