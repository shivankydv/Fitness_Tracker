package com.fitnesstracker.service;

import com.fitnesstracker.domain.Activity;
import com.fitnesstracker.domain.User;
import com.fitnesstracker.domain.enums.ActivityType;
import com.fitnesstracker.dto.ActivityRequest;
import com.fitnesstracker.dto.ActivityResponse;
import com.fitnesstracker.exception.ResourceNotFoundException;
import com.fitnesstracker.repository.ActivityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Transactional
    public ActivityResponse createActivity(User user, ActivityRequest request) {
        validateActivityRequest(request);

        Activity activity = new Activity(
                user,
                request.getActivityType(),
                request.getActivityDate(),
                request.getDurationMinutes(),
                request.getDistanceKm(),
                request.getCaloriesBurned(),
                request.getNotes()
        );

        Activity saved = activityRepository.save(activity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ActivityResponse> getUserActivities(User user, Pageable pageable) {
        return activityRepository.findByUserOrderByActivityDateDesc(user, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getRecentActivities(User user) {
        return activityRepository.findTop10ByUserOrderByActivityDateDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ActivityResponse getActivityForUser(Long id, User user) {
        Activity activity = activityRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + id));
        return toResponse(activity);
    }

    @Transactional
    public ActivityResponse updateActivity(Long id, User user, ActivityRequest request) {
        validateActivityRequest(request);

        Activity activity = activityRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + id));

        activity.setActivityType(request.getActivityType());
        activity.setActivityDate(request.getActivityDate());
        activity.setDurationMinutes(request.getDurationMinutes());
        activity.setDistanceKm(request.getDistanceKm());
        activity.setCaloriesBurned(request.getCaloriesBurned());
        activity.setNotes(request.getNotes());

        Activity updated = activityRepository.save(activity);
        return toResponse(updated);
    }

    @Transactional
    public void deleteActivity(Long id, User user) {
        int deleted = activityRepository.deleteByIdAndUser(id, user);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Activity not found or access denied: " + id);
        }
    }

    @Transactional(readOnly = true)
    public long countUserActivities(User user) {
        return activityRepository.countByUser(user);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStatistics(User user) {
        List<Activity> allActivities = activityRepository.findByUserOrderByActivityDateDesc(user);

        Map<String, Object> stats = new LinkedHashMap<>();

        // Total activities
        stats.put("totalActivities", allActivities.size());

        // Recent activities (last 5)
        List<Activity> recentActivities = allActivities.stream().limit(5).toList();
        stats.put("recentCount", recentActivities.size());
        stats.put("recentActivities", recentActivities.stream().map(this::toResponse).toList());

        // Total calories, distance, minutes
        int totalCalories = allActivities.stream().mapToInt(Activity::getCaloriesBurned).sum();
        BigDecimal totalDistance = allActivities.stream()
                .map(Activity::getDistanceKm)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalMinutes = allActivities.stream().mapToInt(Activity::getDurationMinutes).sum();

        stats.put("totalCalories", totalCalories);
        stats.put("totalDistance", totalDistance);
        stats.put("totalMinutes", totalMinutes);

        // Current streak
        stats.put("streak", calculateStreak(allActivities));

        // Weekly summary
        stats.put("weeklySummary", calculateWeeklySummary(allActivities));

        return stats;
    }

    private int calculateStreak(List<Activity> activities) {
        if (activities.isEmpty()) {
            return 0;
        }

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

    private Map<String, Integer> calculateWeeklySummary(List<Activity> activities) {
        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("SUNDAY", 0);
        summary.put("MONDAY", 0);
        summary.put("TUESDAY", 0);
        summary.put("WEDNESDAY", 0);
        summary.put("THURSDAY", 0);
        summary.put("FRIDAY", 0);
        summary.put("SATURDAY", 0);

        for (Activity activity : activities) {
            String dayName = activity.getActivityDate().getDayOfWeek().name();
            if (summary.containsKey(dayName)) {
                summary.put(dayName, summary.get(dayName) + 1);
            }
        }

        return summary;
    }

    private void validateActivityRequest(ActivityRequest request) {
        if (request.getActivityType() == null) {
            throw new IllegalArgumentException("Activity type is required");
        }
        if (request.getActivityDate() == null) {
            throw new IllegalArgumentException("Activity date is required");
        }
        if (request.getDurationMinutes() == null || request.getDurationMinutes() < 0) {
            throw new IllegalArgumentException("Duration must be non-negative");
        }
        if (request.getDistanceKm() == null || request.getDistanceKm().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Distance must be non-negative");
        }
        if (request.getCaloriesBurned() == null || request.getCaloriesBurned() < 0) {
            throw new IllegalArgumentException("Calories burned must be non-negative");
        }
    }

    private ActivityResponse toResponse(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getActivityType(),
                activity.getActivityDate(),
                activity.getDurationMinutes(),
                activity.getDistanceKm(),
                activity.getCaloriesBurned(),
                activity.getNotes(),
                activity.getCreatedAt()
        );
    }
}