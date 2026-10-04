package com.fitnesstracker.service;

import com.fitnesstracker.domain.Activity;
import com.fitnesstracker.domain.User;
import com.fitnesstracker.domain.enums.ActivityType;
import com.fitnesstracker.repository.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

        // Use repository to filter by date range at database level
        List<Activity> activities = activityRepository.findByUserAndActivityDateBetween(user, startDate, endDate);

        Map<String, Object> stats = new LinkedHashMap<>();

        // Basic calculations
        stats.put("totalWorkouts", activities.size());
        stats.put("totalCalories", calculateTotalCalories(activities));
        stats.put("totalDistance", calculateTotalDistance(activities));
        stats.put("activeMinutes", calculateActiveMinutes(activities));
        stats.put("averageDuration", calculateAverageDuration(activities));
        stats.put("currentStreak", calculateCurrentStreak(activities));
        stats.put("activityDistribution", calculateActivityDistribution(activities));
        stats.put("personalRecords", calculatePersonalRecords(activities));

        return stats;
    }

    private LocalDate calculateStartDate(LocalDate endDate, String range) {
        return switch (range) {
            case "7d" -> endDate.minusDays(7);
            case "30d" -> endDate.minusDays(30);
            case "3m" -> endDate.minusMonths(3);
            case "1y" -> endDate.minusYears(1);
            default -> endDate.minusDays(30);
        };
    }

    private int calculateTotalCalories(List<Activity> activities) {
        return activities.stream()
                .mapToInt(Activity::getCaloriesBurned)
                .sum();
    }

    private BigDecimal calculateTotalDistance(List<Activity> activities) {
        return activities.stream()
                .map(Activity::getDistanceKm)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int calculateActiveMinutes(List<Activity> activities) {
        return activities.stream()
                .mapToInt(Activity::getDurationMinutes)
                .sum();
    }

    private double calculateAverageDuration(List<Activity> activities) {
        if (activities.isEmpty()) return 0.0;
        double total = activities.stream()
                .mapToInt(Activity::getDurationMinutes)
                .average()
                .orElse(0.0);
        return Math.round(total * 10.0) / 10.0;
    }

    private int calculateCurrentStreak(List<Activity> activities) {
        if (activities.isEmpty()) return 0;

        Set<LocalDate> activeDays = activities.stream()
                .map(Activity::getActivityDate)
                .collect(Collectors.toSet());

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
        // Use enum names as keys for consistency
        Map<String, Integer> distribution = new LinkedHashMap<>();
        for (ActivityType type : ActivityType.values()) {
            distribution.put(type.name(), 0);
        }

        for (Activity a : activities) {
            String type = a.getActivityType().name();
            distribution.merge(type, 1, Integer::sum);
        }

        return distribution;
    }

    private Map<String, Object> calculatePersonalRecords(List<Activity> activities) {
        Map<String, Object> prs = new LinkedHashMap<>();

        if (activities.isEmpty()) {
            prs.put("longestWorkout", 0);
            prs.put("longestDistance", BigDecimal.ZERO);
            prs.put("mostCalories", 0);
            prs.put("bestStreak", 0);
            prs.put("longestActivePeriod", 0);
            return prs;
        }

        Optional<Activity> longest = activities.stream()
                .max(Comparator.comparingInt(Activity::getDurationMinutes));
        prs.put("longestWorkout", longest.map(Activity::getDurationMinutes).orElse(0));

        Optional<Activity> longestDist = activities.stream()
                .max(Comparator.comparing(Activity::getDistanceKm));
        prs.put("longestDistance", longestDist.map(Activity::getDistanceKm).orElse(BigDecimal.ZERO));

        Optional<Activity> mostCalories = activities.stream()
                .max(Comparator.comparingInt(Activity::getCaloriesBurned));
        prs.put("mostCalories", mostCalories.map(Activity::getCaloriesBurned).orElse(0));

        // Calculate best streak (longest consecutive days with activity)
        int bestStreak = calculateBestStreak(activities);
        prs.put("bestStreak", bestStreak);

        // Longest active period (consecutive days with at least one activity)
        int longestActivePeriod = calculateLongestActivePeriod(activities);
        prs.put("longestActivePeriod", longestActivePeriod);

        return prs;
    }

    private int calculateBestStreak(List<Activity> activities) {
        if (activities.isEmpty()) return 0;

        Set<LocalDate> activeDays = activities.stream()
                .map(Activity::getActivityDate)
                .collect(Collectors.toSet());

        List<Activity> sorted = new ArrayList<>(activities);
        sorted.sort(Comparator.comparing(Activity::getActivityDate));

        int bestStreak = 0;
        int currentStreak = 0;
        LocalDate prevDay = null;

        for (Activity a : sorted) {
            LocalDate current = a.getActivityDate();
            if (prevDay == null) {
                currentStreak = 1;
            } else if (current.equals(prevDay.plusDays(1))) {
                currentStreak++;
            } else {
                bestStreak = Math.max(bestStreak, currentStreak);
                currentStreak = 1;
            }
            prevDay = current;
        }
        bestStreak = Math.max(bestStreak, currentStreak);

        return bestStreak;
    }

    private int calculateLongestActivePeriod(List<Activity> activities) {
        if (activities.isEmpty()) return 0;

        Set<LocalDate> activeDays = activities.stream()
                .map(Activity::getActivityDate)
                .collect(Collectors.toSet());

        List<LocalDate> sortedDays = new ArrayList<>(activeDays);
        sortedDays.sort(LocalDate::compareTo);

        int longestPeriod = 1;
        int currentPeriod = 1;

        for (int i = 1; i < sortedDays.size(); i++) {
            if (sortedDays.get(i).equals(sortedDays.get(i - 1).plusDays(1))) {
                currentPeriod++;
                longestPeriod = Math.max(longestPeriod, currentPeriod);
            } else {
                currentPeriod = 1;
            }
        }

        return longestPeriod;
    }
}