package com.fitnesstracker.controller;

import com.fitnesstracker.dto.ProfileRequest;
import com.fitnesstracker.dto.UserResponse;
import com.fitnesstracker.domain.User;
import com.fitnesstracker.service.UserService;
import com.fitnesstracker.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
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
        ProfileRequest profileRequest = new ProfileRequest(user.getName(), user.getEmail());
        model.addAttribute("profileRequest", profileRequest);
        return "profile/edit";
    }

    @PostMapping
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid @ModelAttribute("profileRequest") ProfileRequest profileRequest,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            return "profile/edit";
        }

        try {
            userService.updateProfile(userDetails.getUser().getId(), profileRequest.getName(), profileRequest.getEmail());
            redirectAttributes.addFlashAttribute("flash", "Profile updated successfully!");
            redirectAttributes.addFlashAttribute("flashType", "success");
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("email", "error.email", e.getMessage());
            return "profile/edit";
        }
    }
}