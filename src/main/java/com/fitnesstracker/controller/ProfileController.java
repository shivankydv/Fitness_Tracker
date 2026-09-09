package com.fitnesstracker.controller;

import com.fitnesstracker.dto.UserResponse;
import com.fitnesstracker.domain.User;
import com.fitnesstracker.service.UserService;
import com.fitnesstracker.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.*;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // No custom binding needed - we handle validation manually @RequestParam
    }

    @GetMapping
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        User user = userDetails.getUser();
        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        model.addAttribute("user", userResponse);
        return "profile/view";
    }

    @GetMapping("/edit")
    public String showEditForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        User user = userDetails.getUser();
        model.addAttribute("user", user);
        model.addAttribute("email", user.getEmail());
        return "profile/edit";
    }

    @PostMapping
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam("name") String name,
                                @RequestParam("email") String email,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            userService.updateProfile(userDetails.getUser().getId(), name, email);
            redirectAttributes.addFlashAttribute("flash", "Profile updated successfully!");
            redirectAttributes.addFlashAttribute("flashType", "success");
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            User user = userDetails.getUser();
            model.addAttribute("user", user);
            model.addAttribute("email", user.getEmail());
            model.addAttribute("error", e.getMessage());
            return "profile/edit";
        }
    }
}