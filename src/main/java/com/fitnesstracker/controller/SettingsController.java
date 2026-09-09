package com.fitnesstracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.fitnesstracker.domain.User;
import com.fitnesstracker.security.CustomUserDetails;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    @GetMapping
    public String settings(@AuthenticationPrincipal CustomUserDetails userDetails,
                           Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        User user = userDetails.getUser();
        model.addAttribute("username", user.getName());
        model.addAttribute("email", user.getEmail());

        return "settings/index";
    }
}