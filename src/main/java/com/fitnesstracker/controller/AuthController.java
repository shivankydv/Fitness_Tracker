package com.fitnesstracker.controller;

import com.fitnesstracker.dto.RegisterRequest;
import com.fitnesstracker.dto.UserResponse;
import com.fitnesstracker.exception.UserAlreadyExistsException;
import com.fitnesstracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            UserResponse userResponse = userService.registerUser(registerRequest);
            model.addAttribute("user", userResponse);
            return "redirect:/auth/login?registered";
        } catch (UserAlreadyExistsException e) {
            bindingResult.reject("registration.email.duplicate", "Email already registered: " + registerRequest.getEmail());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(Model model, String error, String logout) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been successfully logged out.");
        }
        return "login";
    }
}