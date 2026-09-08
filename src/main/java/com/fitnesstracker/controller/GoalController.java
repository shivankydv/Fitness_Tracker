package com.fitnesstracker.controller;

import com.fitnesstracker.domain.enums.GoalType;
import com.fitnesstracker.dto.GoalRequest;
import com.fitnesstracker.dto.GoalResponse;
import com.fitnesstracker.exception.ResourceNotFoundException;
import com.fitnesstracker.security.CustomUserDetails;
import com.fitnesstracker.service.GoalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @ModelAttribute("flash")
    public String flash(RedirectAttributes redirectAttributes, Model model) {
        if (redirectAttributes != null) {
            Object flash = redirectAttributes.getFlashAttributes().get("flash");
            if (flash != null) {
                return flash.toString();
            }
        }
        return "";
    }

    @ModelAttribute("flashType")
    public String flashType(RedirectAttributes redirectAttributes, Model model) {
        if (redirectAttributes != null) {
            Object flashType = redirectAttributes.getFlashAttributes().get("flashType");
            if (flashType != null) {
                return flashType.toString();
            }
        }
        return "info";
    }

    @GetMapping
    public String listGoals(@AuthenticationPrincipal CustomUserDetails userDetails,
                            Model model,
                            Pageable pageable,
                            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        Page<GoalResponse> page = goalService.getUserGoals(userDetails.getUser(), pageable);
        model.addAttribute("goals", page.getContent());
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        return "goals/list";
    }

    @GetMapping("/new")
    public String showNewForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("goalRequest", new GoalRequest());
        model.addAttribute("goalTypes", GoalType.values());
        return "goals/form";
    }

    @PostMapping
    public String createGoal(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @Valid GoalRequest goalRequest,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("goalTypes", GoalType.values());
            return "goals/form";
        }

        try {
            GoalResponse saved = goalService.createGoal(userDetails.getUser(), goalRequest);
            redirectAttributes.addFlashAttribute("flash", "Goal created successfully!");
            redirectAttributes.addFlashAttribute("flashType", "success");
            return "redirect:/goals";
        } catch (IllegalArgumentException e) {
            model.addAttribute("goalTypes", GoalType.values());
            bindingResult.reject("error", e.getMessage());
            return "goals/form";
        }
    }

    @GetMapping("/{id}")
    public String showGoalDetail(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @PathVariable Long id,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            GoalResponse goal = goalService.getGoalForUser(id, userDetails.getUser());
            model.addAttribute("goal", goal);
            return "goals/detail";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("flash", e.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "error");
            return "redirect:/goals";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            GoalResponse goal = goalService.getGoalForUser(id, userDetails.getUser());
            model.addAttribute("goal", goal);
            model.addAttribute("goalTypes", GoalType.values());
            return "goals/form";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("flash", e.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "error");
            return "redirect:/goals";
        }
    }

    @PostMapping("/{id}")
    public String updateGoal(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @PathVariable Long id,
                             @Valid GoalRequest goalRequest,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("goalTypes", GoalType.values());
            return "goals/form";
        }

        try {
            GoalResponse updated = goalService.updateGoal(id, userDetails.getUser(), goalRequest);
            redirectAttributes.addFlashAttribute("flash", "Goal updated successfully!");
            redirectAttributes.addFlashAttribute("flashType", "success");
            return "redirect:/goals/" + id;
        } catch (IllegalArgumentException e) {
            model.addAttribute("goalTypes", GoalType.values());
            bindingResult.reject("error", e.getMessage());
            return "goals/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteGoal(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        try {
            goalService.deleteGoal(id, userDetails.getUser());
            redirectAttributes.addFlashAttribute("flash", "Goal deleted successfully!");
            redirectAttributes.addFlashAttribute("flashType", "success");
            return "redirect:/goals";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("flash", e.getMessage());
            redirectAttributes.addFlashAttribute("flashType", "error");
            return "redirect:/goals";
        }
    }
}