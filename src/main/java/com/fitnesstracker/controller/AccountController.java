package com.fitnesstracker.controller;

import com.fitnesstracker.service.UserService;
import com.fitnesstracker.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings/account")
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String deleteAccountForm() {
        return "settings/account";
    }

    @PostMapping("/delete")
    public String deleteAccount(@AuthenticationPrincipal CustomUserDetails userDetails,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        // Delete only the authenticated user's account
        // The UserService.deleteUser method handles cascade deletion of owned resources
        Long userId = userDetails.getUser().getId();
        userService.deleteUser(userId);

        // Invalidate session and clear Spring Security context
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());

        // Add flash message for the redirect
        redirectAttributes.addFlashAttribute("flash", "Your account has been successfully deleted.");
        redirectAttributes.addFlashAttribute("flashType", "info");

        // Redirect to public home page
        return "redirect:/";
    }
}