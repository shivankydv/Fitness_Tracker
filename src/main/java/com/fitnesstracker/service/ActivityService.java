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
import java.util.List;

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