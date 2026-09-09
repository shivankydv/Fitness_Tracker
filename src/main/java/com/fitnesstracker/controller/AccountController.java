package com.fitnesstracker.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fitnesstracker.service.UserService;
import com.fitnesstracker.security.CustomUserDetails;

@Controller
@RequestMapping("/settings/account")
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/delete")
    public String deleteAccount(@AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        // Delete only the authenticated user's account
        // The UserService.deleteUser method handles cascade deletion of owned resources
        userService.deleteUser(userDetails.getUser().getId());

        // Invalidate session/authentication
        redirectAttributes.addFlashAttribute("flash", "Your account has been successfully deleted.");
        redirectAttributes.addFlashAttribute("flashType", "error");

        // Redirect to public home page (not login, since user is no longer authenticated)
        return "redirect:/";
    }
}