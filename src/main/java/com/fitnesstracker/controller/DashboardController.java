package com.fitnesstracker.controller;

import com.fitnesstracker.domain.User;
import com.fitnesstracker.domain.enums.ActivityType;
import com.fitnesstracker.dto.ActivityResponse;
import com.fitnesstracker.dto.GoalResponse;
import com.fitnesstracker.security.CustomUserDetails;
import com.fitnesstracker.service.ActivityService;
import com.fitnesstracker.service.GoalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class DashboardController {

    private final ActivityService activityService;
    private final GoalService goalService;

    public DashboardController(ActivityService activityService, GoalService goalService) {
        this.activityService = activityService;
        this.goalService = goalService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails,
                            Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        User user = userDetails.getUser();

        // Get all activity statistics in a single query
        Map<String, Object> activityStats = activityService.getDashboardStatistics(user);

        // Active goals
        List<GoalResponse> activeGoals = goalService.getActiveGoals(user);
        int activeGoalsCount = activeGoals.size();

        // Completed goals
        int completedGoalsCount = (int) goalService.countUserGoals(user);

        // Goal progress percentage - average of all active goals
        double avgGoalProgress = 0;
        if (!activeGoals.isEmpty()) {
            double totalProgress = activeGoals.stream()
                    .mapToDouble(GoalResponse::getProgressPercentage)
                    .sum();
            avgGoalProgress = totalProgress / activeGoals.size();
        }

        model.addAttribute("username", userDetails.getUser().getName());
        model.addAttribute("totalActivities", activityStats.get("totalActivities"));
        model.addAttribute("recentCount", activityStats.get("recentCount"));
        model.addAttribute("totalCalories", activityStats.get("totalCalories"));
        model.addAttribute("totalDistance", activityStats.get("totalDistance"));
        model.addAttribute("totalMinutes", activityStats.get("totalMinutes"));
        model.addAttribute("activeGoalsCount", activeGoalsCount);
        model.addAttribute("completedGoalsCount", completedGoalsCount);
        model.addAttribute("streak", activityStats.get("streak"));
        model.addAttribute("avgGoalProgress", Math.round(avgGoalProgress));
        model.addAttribute("weeklySummary", activityStats.get("weeklySummary"));
        model.addAttribute("recentActivities", activityStats.get("recentActivities"));
        model.addAttribute("activityTypes", ActivityType.values());

        return "dashboard";
    }
}