package com.fitnesstracker.controller;

import com.fitnesstracker.domain.User;
import com.fitnesstracker.domain.enums.ActivityType;
import com.fitnesstracker.dto.ActivityResponse;
import com.fitnesstracker.dto.GoalResponse;
import com.fitnesstracker.exception.ResourceNotFoundException;
import com.fitnesstracker.security.CustomUserDetails;
import com.fitnesstracker.service.ActivityService;
import com.fitnesstracker.service.GoalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.*;
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
                            Model model,
                            Pageable pageable) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        User user = userDetails.getUser();

        // Total workouts (activities count)
        long totalActivities = activityService.countUserActivities(user);

        // Total activities - get recent activities count
        List<ActivityResponse> recentActivities = activityService.getRecentActivities(user);
        int recentCount = Math.min(recentActivities.size(), 5);

        // Calories burned (sum from all activities)
        int totalCalories = 0;
        for (ActivityResponse activity : recentActivities) {
            totalCalories += activity.getCaloriesBurned();
        }

        // Total distance
        BigDecimal totalDistance = BigDecimal.ZERO;
        for (ActivityResponse activity : recentActivities) {
            totalDistance = totalDistance.add(activity.getDistanceKm());
        }

        // Active minutes (from duration)
        int totalMinutes = 0;
        for (ActivityResponse activity : recentActivities) {
            totalMinutes += activity.getDurationMinutes();
        }

        // Active goals
        List<GoalResponse> activeGoals = goalService.getActiveGoals(user);
        int activeGoalsCount = activeGoals.size();

        // Completed goals - count user goals
        int completedGoalsCount;
        try {
            completedGoalsCount = (int) goalService.countUserGoals(user);
        } catch (Exception e) {
            completedGoalsCount = 0;
        }

        // Current workout streak calculation
        int streak = calculateStreak(user, activityService);

        // Goal progress percentage - average of all active goals
        double avgGoalProgress = 0;
        if (!activeGoals.isEmpty()) {
            double totalProgress = activeGoals.stream()
                    .mapToDouble(GoalResponse::getProgressPercentage)
                    .sum();
            avgGoalProgress = totalProgress / activeGoals.size();
        }

        // Weekly activity summary
        Map<String, Integer> weeklySummary = calculateWeeklySummary(user, activityService);

        // Add attributes to model
        model.addAttribute("totalActivities", totalActivities);
        model.addAttribute("recentCount", recentCount);
        model.addAttribute("totalCalories", totalCalories);
        model.addAttribute("totalDistance", totalDistance);
        model.addAttribute("totalMinutes", totalMinutes);
        model.addAttribute("activeGoalsCount", activeGoalsCount);
        model.addAttribute("completedGoalsCount", completedGoalsCount);
        model.addAttribute("streak", streak);
        model.addAttribute("avgGoalProgress", Math.round(avgGoalProgress));
        model.addAttribute("weeklySummary", weeklySummary);
        model.addAttribute("recentActivities", recentActivities);
        model.addAttribute("activityTypes", ActivityType.values());

        return "dashboard::main-content";
    }

    private int calculateStreak(User user, ActivityService activityService) {
        // Calculate the current workout streak (consecutive days with at least one activity)
        // Get all activities sorted by date descending
        List<ActivityResponse> recent = activityService.getRecentActivities(user);
        if (recent.isEmpty()) {
            return 0;
        }

        // Sort by date descending
        recent.sort((a, b) -> b.getActivityDate().compareTo(a.getActivityDate()));

        Set<LocalDate> activeDays = new HashSet<>();
        for (ActivityResponse activity : recent) {
            activeDays.add(activity.getActivityDate());
        }

        // Calculate streak from most recent day backwards
        int streak = 0;
        LocalDate currentDay = LocalDate.now();

        // Count consecutive days from today backwards that have activities
        for (int i = 0; i < 30; i++) {
            if (activeDays.contains(currentDay)) {
                streak++;
            } else {
                break;
            }
            currentDay = currentDay.minusDays(1);
        }

        return streak;
    }

    private Map<String, Integer> calculateWeeklySummary(User user, ActivityService activityService) {
        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("Sunday", 0);
        summary.put("Monday", 0);
        summary.put("Tuesday", 0);
        summary.put("Wednesday", 0);
        summary.put("Thursday", 0);
        summary.put("Friday", 0);
        summary.put("Saturday", 0);

        // Get recent activities and count by day of week
        List<ActivityResponse> recent = activityService.getRecentActivities(user);
        for (ActivityResponse activity : recent) {
            DayOfWeek day = activity.getActivityDate().getDayOfWeek();
            String dayName = day.name();
            if (summary.containsKey(dayName)) {
                summary.put(dayName, summary.get(dayName) + 1);
            }
        }

        return summary;
    }
}