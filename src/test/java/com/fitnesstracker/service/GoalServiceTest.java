package com.fitnesstracker.service;

import com.fitnesstracker.domain.Goal;
import com.fitnesstracker.domain.User;
import com.fitnesstracker.domain.enums.GoalStatus;
import com.fitnesstracker.domain.enums.GoalType;
import com.fitnesstracker.dto.GoalRequest;
import com.fitnesstracker.dto.GoalResponse;
import com.fitnesstracker.exception.ResourceNotFoundException;
import com.fitnesstracker.repository.GoalRepository;
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
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private GoalService goalService;

    private User createUser(Long id, String email) {
        User user = new User("Test User", email, "password123");
        user.setId(id);
        return user;
    }

    private Goal createGoal(Long id, User user) {
        Goal goal = new Goal(user, GoalType.DISTANCE, "Run 100km", new BigDecimal("100"),
                BigDecimal.ZERO, "km", LocalDate.now().plusDays(30), GoalStatus.ACTIVE);
        goal.setId(id);
        goal.setCreatedAt(LocalDateTime.now());
        return goal;
    }

    private GoalRequest createValidRequest() {
        GoalRequest request = new GoalRequest();
        request.setGoalType(GoalType.DISTANCE);
        request.setTitle("Run 100km");
        request.setTargetValue(new BigDecimal("100"));
        request.setCurrentValue(new BigDecimal("100"));
        request.setUnit("km");
        request.setDeadline(LocalDate.now().plusDays(30));
        // Note: status is not set here, allowing the service to determine it based on progress
        return request;
    }

    @Test
    void createGoal_validRequest_createsGoal() {
        User user = createUser(1L, "user@example.com");
        GoalRequest request = createValidRequest();

        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> {
            Goal g = invocation.getArgument(0);
            g.setId(5L);
            g.setCreatedAt(LocalDateTime.now());
            return g;
        });

        GoalResponse response = goalService.createGoal(user, request);

        assertThat(response).isNotNull();
        assertThat(response.getGoalType()).isEqualTo(GoalType.DISTANCE);
        assertThat(response.getTitle()).isEqualTo("Run 100km");
        assertThat(response.getTargetValue()).isEqualByComparingTo("100");
        assertThat(response.getId()).isEqualTo(5L);
    }

    @Test
    void createGoal_nullTitle_throwsException() {
        User user = createUser(1L, "user@example.com");
        GoalRequest request = createValidRequest();
        request.setTitle(null);

        assertThatThrownBy(() -> goalService.createGoal(user, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Title is required");
    }

    @Test
    void createGoal_negativeTarget_throwsException() {
        User user = createUser(1L, "user@example.com");
        GoalRequest request = createValidRequest();
        request.setTargetValue(new BigDecimal("-10"));

        assertThatThrownBy(() -> goalService.createGoal(user, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Target value must be non-negative");
    }

    @Test
    void getUserGoals_returnsPagedResults() {
        User user = createUser(1L, "user@example.com");
        Goal goal1 = createGoal(1L, user);
        Goal goal2 = createGoal(2L, user);
        Page<Goal> page = new PageImpl<>(List.of(goal1, goal2));

        when(goalRepository.findByUserOrderByCreatedAtDesc(eq(user), any(Pageable.class)))
                .thenReturn(page);

        Page<GoalResponse> result = goalService.getUserGoals(user, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void getActiveGoals_returnsActiveGoalsOnly() {
        User user = createUser(1L, "user@example.com");
        Goal activeGoal = createGoal(1L, user);
        activeGoal.setStatus(GoalStatus.ACTIVE);

        when(goalRepository.findByUserAndStatusOrderByDeadlineAsc(eq(user), eq(GoalStatus.ACTIVE)))
                .thenReturn(List.of(activeGoal));

        List<GoalResponse> result = goalService.getActiveGoals(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(GoalStatus.ACTIVE);
    }

    @Test
    void getGoalForUser_ownedGoal_returnsGoal() {
        User user = createUser(1L, "user@example.com");
        Goal goal = createGoal(10L, user);

        when(goalRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(goal));

        GoalResponse response = goalService.getGoalForUser(10L, user);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
    }

    @Test
    void getGoalForUser_otherUserGoal_throwsNotFound() {
        User userA = createUser(1L, "usera@example.com");
        User userB = createUser(2L, "userb@example.com");

        when(goalRepository.findByIdAndUser(10L, userB)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.getGoalForUser(10L, userB))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateGoal_ownedGoal_updatesFields() {
        User user = createUser(1L, "user@example.com");
        Goal existing = createGoal(10L, user);
        GoalRequest request = createValidRequest();
        request.setTargetValue(new BigDecimal("200"));
        request.setStatus(GoalStatus.COMPLETED);

        when(goalRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(existing));
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GoalResponse response = goalService.updateGoal(10L, user, request);

        assertThat(response.getTargetValue()).isEqualByComparingTo("200");
        assertThat(response.getStatus()).isEqualTo(GoalStatus.COMPLETED);
    }

    @Test
    void updateGoal_otherUserGoal_throwsNotFound() {
        User userA = createUser(1L, "usera@example.com");
        User userB = createUser(2L, "userb@example.com");

        when(goalRepository.findByIdAndUser(10L, userB)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.updateGoal(10L, userB, createValidRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteGoal_ownedGoal_deletesSuccessfully() {
        User user = createUser(1L, "user@example.com");

        when(goalRepository.deleteByIdAndUser(10L, user)).thenReturn(1);

        goalService.deleteGoal(10L, user);

        verify(goalRepository).deleteByIdAndUser(10L, user);
    }

    @Test
    void deleteGoal_otherUserGoal_throwsNotFound() {
        User user = createUser(1L, "user@example.com");

        when(goalRepository.deleteByIdAndUser(10L, user)).thenReturn(0);

        assertThatThrownBy(() -> goalService.deleteGoal(10L, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("access denied");
    }

    @Test
    void countUserGoals_returnsCorrectCount() {
        User user = createUser(1L, "user@example.com");

        when(goalRepository.countByUser(user)).thenReturn(3L);

        long count = goalService.countUserGoals(user);

        assertThat(count).isEqualTo(3L);
    }

    @Test
    void createGoal_defaultStatus_isActive() {
        User user = createUser(1L, "user@example.com");
        GoalRequest request = new GoalRequest();
        request.setGoalType(GoalType.DISTANCE);
        request.setTitle("Test Goal");
        request.setTargetValue(new BigDecimal("50"));
        request.setDeadline(LocalDate.now().plusDays(30));

        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> {
            Goal g = invocation.getArgument(0);
            g.setId(1L);
            return g;
        });

        goalService.createGoal(user, request);

        ArgumentCaptor<Goal> goalCaptor = ArgumentCaptor.forClass(Goal.class);
        verify(goalRepository).save(goalCaptor.capture());

        assertThat(goalCaptor.getValue().getStatus()).isEqualTo(GoalStatus.ACTIVE);
    }

    @Test
    void updateGoal_defaultStatus_preservesBasedOnProgress() {
        User user = createUser(1L, "user@example.com");
        Goal existing = createGoal(10L, user);
        existing.setCurrentValue(new BigDecimal("100"));

        GoalRequest request = new GoalRequest();
        request.setGoalType(GoalType.DISTANCE);
        request.setTitle("Updated Goal");
        request.setTargetValue(new BigDecimal("100"));
        request.setCurrentValue(new BigDecimal("100"));  // Set currentValue to match goal
        request.setDeadline(LocalDate.now().plusDays(30));

        when(goalRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(existing));
        when(goalRepository.save(any(Goal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        goalService.updateGoal(10L, user, request);

        assertThat(existing.getStatus()).isEqualTo(GoalStatus.COMPLETED);
    }
}