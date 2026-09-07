package com.backend1.backend1.controller;

import com.backend1.backend1.service.BookingService;
import com.backend1.backend1.service.CustomerService;
import com.backend1.backend1.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CustomerService customerService;
    private final RoomService roomService;
    private final BookingService bookingService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("customerCount", customerService.count());
        model.addAttribute("roomCount", roomService.count());
        model.addAttribute("bookingCount", bookingService.count());
        return "index";
    }
}
