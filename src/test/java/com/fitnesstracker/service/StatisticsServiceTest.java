package com.fitnesstracker.service;

import com.fitnesstracker.domain.Activity;
import com.fitnesstracker.domain.User;
import com.fitnesstracker.domain.enums.ActivityType;
import com.fitnesstracker.repository.ActivityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    private User createUser(Long id, String email) {
        User user = new User("Test User", email, "password123");
        user.setId(id);
        return user;
    }

    private Activity createActivity(Long id, User user, ActivityType type, LocalDate date,
                                    Integer duration, BigDecimal distance, Integer calories) {
        Activity activity = new Activity(user, type, date, duration, distance, calories, "Test");
        activity.setId(id);
        activity.setCreatedAt(LocalDateTime.now());
        return activity;
    }

    @Test
    void getStatistics_emptyUser_returnsZeros() {
        User user = createUser(1L, "user@example.com");
        when(activityRepository.findByUserAndActivityDateBetween(eq(user), any(), any())).thenReturn(List.of());

        Map<String, Object> stats = statisticsService.getStatistics(user, "30d");

        assertThat(stats.get("totalWorkouts")).isEqualTo(0);
        assertThat(stats.get("totalCalories")).isEqualTo(0);
        assertThat((BigDecimal) stats.get("totalDistance")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(stats.get("activeMinutes")).isEqualTo(0);
        assertThat(stats.get("averageDuration")).isEqualTo(0.0);
        assertThat(stats.get("currentStreak")).isEqualTo(0);
        assertThat(stats.get("activityDistribution")).isInstanceOf(Map.class);
        assertThat(stats.get("personalRecords")).isInstanceOf(Map.class);
    }

    @Test
    void getStatistics_singleActivity_returnsCorrectStats() {
        User user = createUser(1L, "user@example.com");
        LocalDate today = LocalDate.now();
        Activity activity = createActivity(1L, user, ActivityType.RUNNING, today, 30, new BigDecimal("5.0"), 300);
        when(activityRepository.findByUserAndActivityDateBetween(eq(user), any(), any())).thenReturn(List.of(activity));

        Map<String, Object> stats = statisticsService.getStatistics(user, "30d");

        assertThat(stats.get("totalWorkouts")).isEqualTo(1);
        assertThat(stats.get("totalCalories")).isEqualTo(300);
        assertThat((BigDecimal) stats.get("totalDistance")).isEqualByComparingTo(new BigDecimal("5.0"));
        assertThat(stats.get("activeMinutes")).isEqualTo(30);
        assertThat(stats.get("averageDuration")).isEqualTo(30.0);
        assertThat(stats.get("currentStreak")).isEqualTo(1);

        Map<String, Integer> distribution = (Map<String, Integer>) stats.get("activityDistribution");
        assertThat(distribution.get("RUNNING")).isEqualTo(1);
        assertThat(distribution.get("WALKING")).isEqualTo(0);

        Map<String, Object> prs = (Map<String, Object>) stats.get("personalRecords");
        assertThat(prs.get("longestWorkout")).isEqualTo(30);
        assertThat((BigDecimal) prs.get("longestDistance")).isEqualByComparingTo(new BigDecimal("5.0"));
        assertThat(prs.get("mostCalories")).isEqualTo(300);
        assertThat(prs.get("bestStreak")).isEqualTo(1);
        assertThat(prs.get("longestActivePeriod")).isEqualTo(1);
    }

    @Test
    void getStatistics_multipleActivityTypes_distributionCorrect() {
        User user = createUser(1L, "user@example.com");
        LocalDate today = LocalDate.now();
        Activity run = createActivity(1L, user, ActivityType.RUNNING, today, 30, new BigDecimal("5.0"), 300);
        Activity walk = createActivity(2L, user, ActivityType.WALKING, today.minusDays(1), 60, new BigDecimal("4.0"), 200);
        Activity cycle = createActivity(3L, user, ActivityType.CYCLING, today.minusDays(2), 45, new BigDecimal("20.0"), 400);

        when(activityRepository.findByUserAndActivityDateBetween(eq(user), any(), any())).thenReturn(List.of(run, walk, cycle));

        Map<String, Object> stats = statisticsService.getStatistics(user, "30d");

        Map<String, Integer> distribution = (Map<String, Integer>) stats.get("activityDistribution");
        assertThat(distribution.get("RUNNING")).isEqualTo(1);
        assertThat(distribution.get("WALKING")).isEqualTo(1);
        assertThat(distribution.get("CYCLING")).isEqualTo(1);
        assertThat(distribution.get("SWIMMING")).isEqualTo(0);
        assertThat(distribution.get("STRENGTH")).isEqualTo(0);
        assertThat(distribution.get("OTHER")).isEqualTo(0);
    }

    @Test
    void getStatistics_decimalDistance_preservesPrecision() {
        User user = createUser(1L, "user@example.com");
        LocalDate today = LocalDate.now();
        Activity activity = createActivity(1L, user, ActivityType.RUNNING, today, 30, new BigDecimal("5.75"), 300);
        when(activityRepository.findByUserAndActivityDateBetween(eq(user), any(), any())).thenReturn(List.of(activity));

        Map<String, Object> stats = statisticsService.getStatistics(user, "30d");

        assertThat((BigDecimal) stats.get("totalDistance")).isEqualByComparingTo(new BigDecimal("5.75"));

        Map<String, Object> prs = (Map<String, Object>) stats.get("personalRecords");
        assertThat((BigDecimal) prs.get("longestDistance")).isEqualByComparingTo(new BigDecimal("5.75"));
    }

    @Test
    void getStatistics_streakCalculation_correct() {
        User user = createUser(1L, "user@example.com");
        LocalDate today = LocalDate.now();
        Activity a1 = createActivity(1L, user, ActivityType.RUNNING, today, 30, new BigDecimal("5.0"), 300);
        Activity a2 = createActivity(2L, user, ActivityType.RUNNING, today.minusDays(1), 30, new BigDecimal("5.0"), 300);
        Activity a3 = createActivity(3L, user, ActivityType.RUNNING, today.minusDays(2), 30, new BigDecimal("5.0"), 300);

        when(activityRepository.findByUserAndActivityDateBetween(eq(user), any(), any())).thenReturn(List.of(a1, a2, a3));

        Map<String, Object> stats = statisticsService.getStatistics(user, "30d");

        assertThat(stats.get("currentStreak")).isEqualTo(3);
    }

    @Test
    void getStatistics_longestActivePeriod_correct() {
        User user = createUser(1L, "user@example.com");
        LocalDate today = LocalDate.now();
        // 3 consecutive days: today, today-1, today-2
        Activity a1 = createActivity(1L, user, ActivityType.RUNNING, today, 30, new BigDecimal("5.0"), 300);
        Activity a2 = createActivity(2L, user, ActivityType.RUNNING, today.minusDays(1), 30, new BigDecimal("5.0"), 300);
        Activity a3 = createActivity(3L, user, ActivityType.RUNNING, today.minusDays(2), 30, new BigDecimal("5.0"), 300);
        // Gap
        Activity a4 = createActivity(4L, user, ActivityType.RUNNING, today.minusDays(10), 30, new BigDecimal("5.0"), 300);

        when(activityRepository.findByUserAndActivityDateBetween(eq(user), any(), any())).thenReturn(List.of(a1, a2, a3, a4));

        Map<String, Object> stats = statisticsService.getStatistics(user, "30d");

        Map<String, Object> prs = (Map<String, Object>) stats.get("personalRecords");
        assertThat(prs.get("longestActivePeriod")).isEqualTo(3); // 3 consecutive days
    }

    @Test
    void getStatistics_dateRangeFiltering_works() {
        User user = createUser(1L, "user@example.com");
        LocalDate today = LocalDate.now();
        Activity recent = createActivity(1L, user, ActivityType.RUNNING, today, 30, new BigDecimal("5.0"), 300);
        Activity old = createActivity(2L, user, ActivityType.RUNNING, today.minusDays(60), 30, new BigDecimal("5.0"), 300);

        when(activityRepository.findByUserAndActivityDateBetween(eq(user), eq(today.minusDays(7)), eq(today))).thenReturn(List.of(recent));

        Map<String, Object> stats = statisticsService.getStatistics(user, "7d");

        assertThat(stats.get("totalWorkouts")).isEqualTo(1);
    }

    @Test
    void getStatistics_bestStreak_correct() {
        User user = createUser(1L, "user@example.com");
        LocalDate today = LocalDate.now();
        // 3 consecutive days, then gap, then 2 consecutive
        Activity a1 = createActivity(1L, user, ActivityType.RUNNING, today, 30, new BigDecimal("5.0"), 300);
        Activity a2 = createActivity(2L, user, ActivityType.RUNNING, today.minusDays(1), 30, new BigDecimal("5.0"), 300);
        Activity a3 = createActivity(3L, user, ActivityType.RUNNING, today.minusDays(2), 30, new BigDecimal("5.0"), 300);
        Activity a4 = createActivity(4L, user, ActivityType.RUNNING, today.minusDays(5), 30, new BigDecimal("5.0"), 300);
        Activity a5 = createActivity(5L, user, ActivityType.RUNNING, today.minusDays(6), 30, new BigDecimal("5.0"), 300);

        when(activityRepository.findByUserAndActivityDateBetween(eq(user), any(), any())).thenReturn(List.of(a1, a2, a3, a4, a5));

        Map<String, Object> stats = statisticsService.getStatistics(user, "30d");

        Map<String, Object> prs = (Map<String, Object>) stats.get("personalRecords");
        assertThat(prs.get("bestStreak")).isEqualTo(3); // longest streak is 3 days
    }

    @Test
    void getStatistics_personalRecords_longestDistancePreservesPrecision() {
        User user = createUser(1L, "user@example.com");
        LocalDate today = LocalDate.now();
        Activity a1 = createActivity(1L, user, ActivityType.RUNNING, today, 30, new BigDecimal("10.25"), 300);
        Activity a2 = createActivity(2L, user, ActivityType.RUNNING, today.minusDays(1), 45, new BigDecimal("5.50"), 400);

        when(activityRepository.findByUserAndActivityDateBetween(eq(user), any(), any())).thenReturn(List.of(a1, a2));

        Map<String, Object> stats = statisticsService.getStatistics(user, "30d");

        Map<String, Object> prs = (Map<String, Object>) stats.get("personalRecords");
        assertThat((BigDecimal) prs.get("longestDistance")).isEqualByComparingTo(new BigDecimal("10.25"));
    }
}