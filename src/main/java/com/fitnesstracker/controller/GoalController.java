package com.fitnesstracker.controller;

import com.fitnesstracker.domain.enums.GoalStatus;
import com.fitnesstracker.domain.enums.GoalType;
import com.fitnesstracker.dto.GoalRequest;
import com.fitnesstracker.dto.GoalResponse;
import com.fitnesstracker.security.CustomUserDetails;
import com.fitnesstracker.service.GoalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

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
                            @RequestParam(required = false) GoalStatus status,
                            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        // Enforce maximum page size to prevent abuse
        if (pageable.getPageSize() > 50) {
            pageable = PageRequest.of(pageable.getPageNumber(), 50, pageable.getSort());
        }

        Page<GoalResponse> page;
        if (status != null) {
            page = goalService.getUserGoalsByStatus(userDetails.getUser(), status, pageable);
        } else {
            page = goalService.getUserGoals(userDetails.getUser(), pageable);
        }

        model.addAttribute("goals", page.getContent());
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("currentStatus", status);
        return "goals/list";
    }

    @GetMapping("/new")
    public String showNewForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("goal", new GoalResponse());
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
            model.addAttribute("goal", new GoalResponse());
            model.addAttribute("goalTypes", GoalType.values());
            return "goals/form";
        }

        GoalResponse saved = goalService.createGoal(userDetails.getUser(), goalRequest);
        redirectAttributes.addFlashAttribute("flash", "Goal created successfully!");
        redirectAttributes.addFlashAttribute("flashType", "success");
        return "redirect:/goals";
    }

    @GetMapping("/{id}")
    public String showGoalDetail(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @PathVariable Long id,
                                 Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        GoalResponse goal = goalService.getGoalForUser(id, userDetails.getUser());
        model.addAttribute("goal", goal);
        return "goals/detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Long id,
                               Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        GoalResponse goal = goalService.getGoalForUser(id, userDetails.getUser());
        model.addAttribute("goal", goal);
        GoalRequest request = new GoalRequest();
        request.setGoalType(goal.getGoalType());
        request.setTitle(goal.getTitle());
        request.setTargetValue(goal.getTargetValue());
        request.setCurrentValue(goal.getCurrentValue());
        request.setUnit(goal.getUnit());
        request.setDeadline(goal.getDeadline());
        request.setStatus(goal.getStatus());
        model.addAttribute("goalRequest", request);
        model.addAttribute("goalTypes", GoalType.values());
        return "goals/form";
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
            model.addAttribute("goal", goalService.getGoalForUser(id, userDetails.getUser()));
            model.addAttribute("goalTypes", GoalType.values());
            return "goals/form";
        }

        GoalResponse updated = goalService.updateGoal(id, userDetails.getUser(), goalRequest);
        redirectAttributes.addFlashAttribute("flash", "Goal updated successfully!");
        redirectAttributes.addFlashAttribute("flashType", "success");
        return "redirect:/goals/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteGoal(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        goalService.deleteGoal(id, userDetails.getUser());
        redirectAttributes.addFlashAttribute("flash", "Goal deleted successfully!");
        redirectAttributes.addFlashAttribute("flashType", "success");
        return "redirect:/goals";
    }
}