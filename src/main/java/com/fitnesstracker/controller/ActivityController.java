package com.fitnesstracker.controller;

import com.fitnesstracker.domain.enums.ActivityType;
import com.fitnesstracker.dto.ActivityRequest;
import com.fitnesstracker.dto.ActivityResponse;
import com.fitnesstracker.exception.ResourceNotFoundException;
import com.fitnesstracker.security.CustomUserDetails;
import com.fitnesstracker.service.ActivityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public String listActivities(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model,
                                 Pageable pageable) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        Page<ActivityResponse> page = activityService.getUserActivities(
                userDetails.getUser(), pageable);
        model.addAttribute("activities", page.getContent());
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        return "activities/list";
    }

    @GetMapping("/new")
    public String showNewForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("activityRequest", new ActivityRequest());
        model.addAttribute("activityTypes", ActivityType.values());
        return "activities/form";
    }

    @PostMapping
    public String createActivity(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid ActivityRequest activityRequest,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("activityTypes", ActivityType.values());
            return "activities/form";
        }

        try {
            ActivityResponse saved = activityService.createActivity(
                    userDetails.getUser(), activityRequest);
            redirectAttributes.addFlashAttribute("flash", "Activity created successfully!");
            redirectAttributes.addFlashAttribute("flashType", "success");
            return "redirect:/activities";
        } catch (IllegalArgumentException e) {
            model.addAttribute("activityTypes", ActivityType.values());
            bindingResult.reject("error", e.getMessage());
            return "activities/form";
        }
    }

    @GetMapping("/{id}")
    public String showActivityDetail(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @PathVariable Long id,
                                     Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            ActivityResponse activity = activityService.getActivityForUser(id, userDetails.getUser());
            model.addAttribute("activity", activity);
            return "activities/detail";
        } catch (ResourceNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Long id,
                               Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            ActivityResponse activity = activityService.getActivityForUser(id, userDetails.getUser());
            model.addAttribute("activity", activity);
            model.addAttribute("activityTypes", ActivityType.values());
            return "activities/form";
        } catch (ResourceNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }

    @PostMapping("/{id}")
    public String updateActivity(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @PathVariable Long id,
                                 @Valid ActivityRequest activityRequest,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("activityTypes", ActivityType.values());
            return "activities/form";
        }

        try {
            ActivityResponse updated = activityService.updateActivity(id, userDetails.getUser(), activityRequest);
            redirectAttributes.addFlashAttribute("flash", "Activity updated successfully!");
            redirectAttributes.addFlashAttribute("flashType", "success");
            return "redirect:/activities/" + id;
        } catch (IllegalArgumentException e) {
            model.addAttribute("activityTypes", ActivityType.values());
            bindingResult.reject("error", e.getMessage());
            return "activities/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteActivity(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            activityService.deleteActivity(id, userDetails.getUser());
            redirectAttributes.addFlashAttribute("flash", "Activity deleted successfully!");
            redirectAttributes.addFlashAttribute("flashType", "success");
            return "redirect:/activities";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("flash", e.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "error");
            return "redirect:/activities";
        }
    }
}