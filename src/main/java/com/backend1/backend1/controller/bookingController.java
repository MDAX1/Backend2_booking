package com.backend1.backend1.controller;

import com.backend1.backend1.exception.BookingConflictException;
import com.backend1.backend1.exception.BookingValidationException;
import com.backend1.backend1.form.SearchForm;
import com.backend1.backend1.service.BookingService;
import com.backend1.backend1.service.RoomService;
import com.backend1.backend1.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final SearchService searchService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("bookings", bookingService.findAll());
        return "bookings/list";
    }

    @GetMapping("/new")
    public String showCreateForm(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false, defaultValue = "1") int guests,
            Model model) {
        model.addAttribute("rooms", roomService.findAll());
        model.addAttribute("selectedRoomId", roomId);
        model.addAttribute("selectedCheckIn", checkIn);
        model.addAttribute("selectedCheckOut", checkOut);
        model.addAttribute("selectedGuests", guests);
        model.addAttribute("pageTitle", "Ny bokning");
        return "bookings/form";
    }

    @PostMapping
    public String create(
            @RequestParam Long customerId,
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") int numberOfGuests,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            bookingService.save(null, customerId, roomId, checkIn, checkOut, numberOfGuests);
            redirectAttributes.addFlashAttribute("successMessage", "Bokningen skapades.");
            return "redirect:/bookings";
        } catch (BookingValidationException | BookingConflictException e) {
            return bookingFormWithError(e.getMessage(), null, customerId, roomId,
                    checkIn, checkOut, numberOfGuests, model);
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        var b = bookingService.findById(id);
        model.addAttribute("bookingId", id);
        model.addAttribute("rooms", roomService.findAll());
        model.addAttribute("selectedCustomerId", b.getCustomerId());
        model.addAttribute("selectedRoomId", b.getRoomId());
        model.addAttribute("selectedCheckIn", b.getCheckIn());
        model.addAttribute("selectedCheckOut", b.getCheckOut());
        model.addAttribute("selectedGuests", b.getNumberOfGuests());
        model.addAttribute("pageTitle", "Redigera bokning");
        return "bookings/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @RequestParam Long customerId,
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") int numberOfGuests,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            bookingService.save(id, customerId, roomId, checkIn, checkOut, numberOfGuests);
            redirectAttributes.addFlashAttribute("successMessage", "Bokningen uppdaterades.");
            return "redirect:/bookings";
        } catch (BookingValidationException | BookingConflictException e) {
            return bookingFormWithError(e.getMessage(), id, customerId, roomId,
                    checkIn, checkOut, numberOfGuests, model);
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookingService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Bokningen avbokades.");
        return "redirect:/bookings";
    }

    @GetMapping("/search")
    public String showSearch(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate preCheckIn,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate preCheckOut,
            @RequestParam(required = false) Integer preGuests,
            Model model) {

        SearchForm form = new SearchForm();
        if (preCheckIn != null) form.setCheckIn(preCheckIn);
        if (preCheckOut != null) form.setCheckOut(preCheckOut);
        if (preGuests != null) form.setNumberOfGuests(preGuests);

        model.addAttribute("searchForm", form);
        return "bookings/search";
    }

    @PostMapping("/search")
    public String search(
            @Valid @ModelAttribute SearchForm searchForm,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) return "bookings/search";

        if (!searchForm.getCheckOut().isAfter(searchForm.getCheckIn())) {
            result.rejectValue("checkOut", "invalid",
                    "Utcheckningsdatum måste vara efter incheckningsdatum");
            return "bookings/search";
        }

        var available = searchService.findAvailableRooms(
                searchForm.getCheckIn(),
                searchForm.getCheckOut(),
                searchForm.getNumberOfGuests());

        model.addAttribute("availableRooms", available);
        model.addAttribute("checkIn", searchForm.getCheckIn());
        model.addAttribute("checkOut", searchForm.getCheckOut());
        model.addAttribute("numberOfGuests", searchForm.getNumberOfGuests());
        return "bookings/search";
    }

    private String bookingFormWithError(String error, Long bookingId, Long customerId,
                                        Long roomId, LocalDate checkIn, LocalDate checkOut, int guests, Model model) {
        model.addAttribute("errorMessage", error);
        model.addAttribute("bookingId", bookingId);
        model.addAttribute("rooms", roomService.findAll());
        model.addAttribute("selectedCustomerId", customerId);
        model.addAttribute("selectedRoomId", roomId);
        model.addAttribute("selectedCheckIn", checkIn);
        model.addAttribute("selectedCheckOut", checkOut);
        model.addAttribute("selectedGuests", guests);
        model.addAttribute("pageTitle", bookingId == null ? "Ny bokning" : "Redigera bokning");
        return "bookings/form";
    }
}
