package com.backend1.backend1.controller;

import com.backend1.backend1.client.AuthClient;
import com.backend1.backend1.config.SessionBearerTokenResolver;
import com.backend1.backend1.exception.CustomerServiceUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final AuthClient authClient;

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletRequest request,
                        Model model) {
        try {
            String token = authClient.login(username, password);
            request.getSession(true)
                    .setAttribute(SessionBearerTokenResolver.SESSION_TOKEN_KEY, token);
            return "redirect:/bookings";
        } catch (HttpClientErrorException.Unauthorized e) {
            model.addAttribute("errorMessage", "Fel användarnamn eller lösenord");
            return "login";
        } catch (CustomerServiceUnavailableException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }
}
