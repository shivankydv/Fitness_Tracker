package com.fitnesstracker.service;

import com.fitnesstracker.domain.Activity;
import com.fitnesstracker.domain.User;
import com.fitnesstracker.domain.enums.ActivityType;
import com.fitnesstracker.dto.ActivityRequest;
import com.fitnesstracker.dto.ActivityResponse;
import com.fitnesstracker.exception.ResourceNotFoundException;
import com.fitnesstracker.repository.ActivityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ActivityService activityService;

    private User createUser(Long id, String email) {
        User user = new User("Test User", email, "password123");
        user.setId(id);
        return user;
    }

    private ActivityRequest createValidRequest() {
        ActivityRequest request = new ActivityRequest();
        request.setActivityType(ActivityType.RUNNING);
        request.setActivityDate(LocalDate.now());
        request.setDurationMinutes(30);
        request.setDistanceKm(new BigDecimal("5.0"));
        request.setCaloriesBurned(300);
        request.setNotes("Test run");
        return request;
    }

    private Activity createActivity(Long id, User user) {
        Activity activity = new Activity(user, ActivityType.RUNNING, LocalDate.now(), 30,
                new BigDecimal("5.0"), 300, "Test run");
        activity.setId(id);
        activity.setCreatedAt(LocalDateTime.now());
        return activity;
    }

    @Test
    void createActivity_validRequest_createsActivity() {
        User user = createUser(1L, "user@example.com");
        ActivityRequest request = createValidRequest();

        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity a = invocation.getArgument(0);
            a.setId(10L);
            a.setCreatedAt(LocalDateTime.now());
            return a;
        });

        ActivityResponse response = activityService.createActivity(user, request);

        assertThat(response).isNotNull();
        assertThat(response.getActivityType()).isEqualTo(ActivityType.RUNNING);
        assertThat(response.getDurationMinutes()).isEqualTo(30);
        assertThat(response.getDistanceKm()).isEqualByComparingTo("5.0");
        assertThat(response.getCaloriesBurned()).isEqualTo(300);
    }

    @Test
    void createActivity_nullActivityType_throwsException() {
        User user = createUser(1L, "user@example.com");
        ActivityRequest request = createValidRequest();
        request.setActivityType(null);

        assertThatThrownBy(() -> activityService.createActivity(user, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Activity type is required");
    }

    @Test
    void createActivity_negativeDuration_throwsException() {
        User user = createUser(1L, "user@example.com");
        ActivityRequest request = createValidRequest();
        request.setDurationMinutes(-10);

        assertThatThrownBy(() -> activityService.createActivity(user, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duration must be non-negative");
    }

    @Test
    void getUserActivities_returnsPagedResults() {
        User user = createUser(1L, "user@example.com");
        Activity activity1 = createActivity(1L, user);
        Activity activity2 = createActivity(2L, user);
        Page<Activity> page = new PageImpl<>(List.of(activity1, activity2));

        when(activityRepository.findByUserOrderByActivityDateDesc(eq(user), any(Pageable.class)))
                .thenReturn(page);

        Page<ActivityResponse> result = activityService.getUserActivities(user, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void getRecentActivities_returnsLimitedResults() {
        User user = createUser(1L, "user@example.com");
        Activity activity1 = createActivity(1L, user);
        Activity activity2 = createActivity(2L, user);

        when(activityRepository.findTop10ByUserOrderByActivityDateDesc(user))
                .thenReturn(List.of(activity1, activity2));

        List<ActivityResponse> result = activityService.getRecentActivities(user);

        assertThat(result).hasSize(2);
    }

    @Test
    void getActivityForUser_ownedActivity_returnsActivity() {
        User user = createUser(1L, "user@example.com");
        Activity activity = createActivity(10L, user);

        when(activityRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(activity));

        ActivityResponse response = activityService.getActivityForUser(10L, user);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
    }

    @Test
    void getActivityForUser_otherUserActivity_throwsNotFound() {
        User userA = createUser(1L, "usera@example.com");
        User userB = createUser(2L, "userb@example.com");

        when(activityRepository.findByIdAndUser(10L, userB)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityService.getActivityForUser(10L, userB))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateActivity_ownedActivity_updatesFields() {
        User user = createUser(1L, "user@example.com");
        Activity existing = createActivity(10L, user);
        ActivityRequest request = createValidRequest();
        request.setActivityType(ActivityType.WALKING);
        request.setDurationMinutes(45);
        request.setNotes("Updated walk");

        when(activityRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(existing));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActivityResponse response = activityService.updateActivity(10L, user, request);

        assertThat(response.getActivityType()).isEqualTo(ActivityType.WALKING);
        assertThat(response.getDurationMinutes()).isEqualTo(45);
        assertThat(response.getNotes()).isEqualTo("Updated walk");

        verify(activityRepository).save(argThat(a ->
                a.getUser().getId().equals(user.getId())
        ));
    }

    @Test
    void updateActivity_otherUserActivity_throwsNotFound() {
        User userA = createUser(1L, "usera@example.com");
        User userB = createUser(2L, "userb@example.com");

        when(activityRepository.findByIdAndUser(10L, userB)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityService.updateActivity(10L, userB, createValidRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteActivity_ownedActivity_deletesSuccessfully() {
        User user = createUser(1L, "user@example.com");

        when(activityRepository.deleteByIdAndUser(10L, user)).thenReturn(1);

        activityService.deleteActivity(10L, user);

        verify(activityRepository).deleteByIdAndUser(10L, user);
    }

    @Test
    void deleteActivity_otherUserActivity_throwsNotFound() {
        User user = createUser(1L, "user@example.com");

        when(activityRepository.deleteByIdAndUser(10L, user)).thenReturn(0);

        assertThatThrownBy(() -> activityService.deleteActivity(10L, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("access denied");
    }

    @Test
    void countUserActivities_returnsCorrectCount() {
        User user = createUser(1L, "user@example.com");

        when(activityRepository.countByUser(user)).thenReturn(5L);

        long count = activityService.countUserActivities(user);

        assertThat(count).isEqualTo(5L);
    }
}