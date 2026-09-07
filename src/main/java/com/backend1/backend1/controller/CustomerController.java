package com.backend1.backend1.controller;

import com.backend1.backend1.form.CustomerForm;
import com.backend1.backend1.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("customers", customerService.findAll());
        return "customers/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("customer", new CustomerForm());
        model.addAttribute("pageTitle", "Ny kund");
        return "customers/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("customer") CustomerForm customer,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Ny kund");
            return "customers/form";
        }

        customerService.save(customer);
        redirectAttributes.addFlashAttribute("successMessage", "Kunden skapades.");
        return "redirect:/customers";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        var dto = customerService.findById(id);
        CustomerForm form = new CustomerForm();
        form.setId(dto.getId());
        form.setFirstName(dto.getFirstName());
        form.setLastName(dto.getLastName());
        form.setEmail(dto.getEmail());
        form.setPhone(dto.getPhone());
        form.setAddress(dto.getAddress());
        model.addAttribute("customer", form);
        model.addAttribute("pageTitle", "Redigera kund");
        return "customers/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("customer") CustomerForm customer,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            customer.setId(id);
            model.addAttribute("pageTitle", "Redigera kund");
            return "customers/form";
        }

        customer.setId(id);
        customerService.save(customer);
        redirectAttributes.addFlashAttribute("successMessage", "Kundens uppgifter uppdaterades.");
        return "redirect:/customers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Kunden togs bort.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/customers";
    }
}