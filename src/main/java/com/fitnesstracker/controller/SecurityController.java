package com.fitnesstracker.controller;

import com.fitnesstracker.service.UserService;
import com.fitnesstracker.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
@RequestMapping("/settings/security")
public class SecurityController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public SecurityController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String changePasswordForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("currentPassword", "");
        return "security/change-password";
    }

    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("currentPassword", currentPassword);
            model.addAttribute("error", "New passwords do not match");
            return "security/change-password";
        }

        if (newPassword.length() < 8) {
            model.addAttribute("currentPassword", currentPassword);
            model.addAttribute("error", "New password must be at least 8 characters");
            return "security/change-password";
        }

        try {
            userService.changePassword(userDetails.getUser().getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("flash", "Password changed successfully!");
            redirectAttributes.addFlashAttribute("flashType", "success");
            return "redirect:/settings";
        } catch (IllegalArgumentException e) {
            model.addAttribute("currentPassword", currentPassword);
            model.addAttribute("error", e.getMessage());
            return "security/change-password";
        }
    }
}