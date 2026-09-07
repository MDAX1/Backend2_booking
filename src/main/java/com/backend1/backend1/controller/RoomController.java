package com.backend1.backend1.controller;

import com.backend1.backend1.form.RoomForm;
import com.backend1.backend1.model.RoomType;
import com.backend1.backend1.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        return "rooms/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("room", new RoomForm());
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("pageTitle", "Nytt rum");
        return "rooms/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("room") RoomForm room,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("roomTypes", RoomType.values());
            model.addAttribute("pageTitle", "Nytt rum");
            return "rooms/form";
        }

        roomService.save(room);
        redirectAttributes.addFlashAttribute("successMessage", "Rummet skapades.");
        return "redirect:/rooms";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        var dto = roomService.findById(id);
        RoomForm form = new RoomForm();
        form.setId(dto.getId());
        form.setRoomNumber(dto.getRoomNumber());
        form.setType(dto.getType());
        form.setExtraBeds(dto.getExtraBeds());
        form.setPricePerNight(dto.getPricePerNight());
        model.addAttribute("room", form);
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("pageTitle", "Redigera rum");
        return "rooms/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("room") RoomForm room,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            room.setId(id);
            model.addAttribute("roomTypes", RoomType.values());
            model.addAttribute("pageTitle", "Redigera rum");
            return "rooms/form";
        }

        room.setId(id);
        roomService.save(room);
        redirectAttributes.addFlashAttribute("successMessage", "Rummet uppdaterades.");
        return "redirect:/rooms";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Rummet togs bort.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/rooms";
    }
}
