package com.fitnesstracker.service;

import com.fitnesstracker.domain.User;
import com.fitnesstracker.domain.Activity;
import com.fitnesstracker.domain.enums.ActivityType;
import com.fitnesstracker.dto.ActivityResponse;
import com.fitnesstracker.repository.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.*;

@Service
public class StatisticsService {

    private final ActivityRepository activityRepository;

    public StatisticsService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics(User user, String range) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(endDate, range);

        List<Activity> activities = activityRepository.findByUserOrderByActivityDateDesc(user);

        // Filter by date range
        List<Activity> filtered = activities.stream()
                .filter(a -> !a.getActivityDate().isBefore(startDate))
                .collect(Collectors.toList());

        Map<String, Object> stats = new LinkedHashMap<>();

        // Basic calculations
        stats.put("totalWorkouts", filtered.size());
        stats.put("totalCalories", calculateTotalCalories(filtered));
        stats.put("totalDistance", calculateTotalDistance(filtered));
        stats.put("activeMinutes", calculateActiveMinutes(filtered));
        stats.put("averageDuration", calculateAverageDuration(filtered));
        stats.put("currentStreak", calculateStreak(filtered));
        stats.put("activityDistribution", calculateActivityDistribution(filtered));
        stats.put("personalRecords", calculatePersonalRecords(filtered));

        return stats;
    }

    private LocalDate calculateStartDate(LocalDate endDate, String range) {
        switch (range) {
            case "7d":
                return endDate.minusDays(7);
            case "30d":
                return endDate.minusDays(30);
            case "3m":
                return endDate.minusMonths(3);
            case "1y":
                return endDate.minusYears(1);
            default:
                return endDate.minusDays(30);
        }
    }

    private int calculateTotalCalories(List<Activity> activities) {
        return activities.stream()
                .mapToInt(a -> a.getCaloriesBurned())
                .sum();
    }

    private BigDecimal calculateTotalDistance(List<Activity> activities) {
        BigDecimal total = BigDecimal.ZERO;
        for (Activity a : activities) {
            total = total.add(a.getDistanceKm());
        }
        return total;
    }

    private int calculateActiveMinutes(List<Activity> activities) {
        return activities.stream()
                .mapToInt(a -> a.getDurationMinutes())
                .sum();
    }

    private double calculateAverageDuration(List<Activity> activities) {
        if (activities.isEmpty()) return 0.0;
        double total = activities.stream()
                .mapToInt(a -> a.getDurationMinutes())
                .average()
                .orElse(0.0);
        return Math.round(total * 10.0) / 10.0;
    }

    private int calculateStreak(List<Activity> activities) {
        if (activities.isEmpty()) return 0;

        List<Activity> sorted = new ArrayList<>(activities);
        sorted.sort((a, b) -> b.getActivityDate().compareTo(a.getActivityDate()));

        Set<LocalDate> activeDays = new HashSet<>();
        for (Activity a : sorted) {
            activeDays.add(a.getActivityDate());
        }

        int streak = 0;
        LocalDate currentDay = LocalDate.now();
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

    private Map<String, Integer> calculateActivityDistribution(List<Activity> activities) {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("Running", 0);
        distribution.put("Walking", 0);
        distribution.put("Cycling", 0);
        distribution.put("Swimming", 0);
        distribution.put("Strength", 0);
        distribution.put("Other", 0);

        for (Activity a : activities) {
            String type = a.getActivityType().name();
            if (distribution.containsKey(type)) {
                distribution.put(type, distribution.get(type) + 1);
            }
        }

        return distribution;
    }

    private Map<String, Integer> calculatePersonalRecords(List<Activity> activities) {
        Map<String, Integer> prs = new LinkedHashMap<>();

        if (activities.isEmpty()) {
            prs.put("longestWorkout", 0);
            prs.put("longestDistance", 0);
            prs.put("mostCalories", 0);
            prs.put("bestStreak", 0);
            prs.put("mostActivePeriod", 0);
            return prs;
        }

        Optional<Activity> longest = activities.stream()
                .max(Comparator.comparingInt(a -> a.getDurationMinutes()));
        prs.put("longestWorkout", longest.map(Activity::getDurationMinutes).orElse(0));

        Optional<Activity> longestDist = activities.stream()
                .max(Comparator.comparing(a -> a.getDistanceKm().doubleValue()));
        prs.put("longestDistance", longestDist.map(a -> a.getDistanceKm().intValue()).orElse(0));

        Optional<Activity> mostCalories = activities.stream()
                .max(Comparator.comparingInt(a -> a.getCaloriesBurned()));
        prs.put("mostCalories", mostCalories.map(Activity::getCaloriesBurned).orElse(0));

        Set<LocalDate> activeDays = new HashSet<>();
        for (Activity a : activities) {
            activeDays.add(a.getActivityDate());
        }
        int bestStreak = 0;
        int currentStreak = 0;
        LocalDate prevDay = null;
        List<Activity> sorted = new ArrayList<>(activities);
        sorted.sort(Comparator.comparing(Activity::getActivityDate));
        for (Activity a : sorted) {
            LocalDate current = a.getActivityDate();
            if (prevDay == null) {
                currentStreak = 1;
            } else if (current.equals(prevDay.minusDays(1))) {
                currentStreak++;
            } else {
                bestStreak = Math.max(bestStreak, currentStreak);
                currentStreak = 1;
            }
            prevDay = current;
        }
        bestStreak = Math.max(bestStreak, currentStreak);
        prs.put("bestStreak", bestStreak);

        int mostActivePeriod = 0;
        if (activities.size() >= 7) {
            for (int i = 0; i <= activities.size() - 7; i++) {
                List<Activity> window = sorted.subList(i, Math.min(i + 7, sorted.size()));
                mostActivePeriod = Math.max(mostActivePeriod, window.size());
            }
        }
        prs.put("mostActivePeriod", mostActivePeriod);

        return prs;
    }
}