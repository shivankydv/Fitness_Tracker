package com.fitnesstracker.service;

import com.fitnesstracker.domain.Goal;
import com.fitnesstracker.domain.User;
import com.fitnesstracker.domain.enums.GoalStatus;
import com.fitnesstracker.domain.enums.GoalType;
import com.fitnesstracker.dto.GoalRequest;
import com.fitnesstracker.dto.GoalResponse;
import com.fitnesstracker.exception.ResourceNotFoundException;
import com.fitnesstracker.repository.GoalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class GoalService {

    private final GoalRepository goalRepository;

    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    @Transactional
    public GoalResponse createGoal(User user, GoalRequest request) {
        validateGoalRequest(request);

        Goal goal = new Goal(
                user,
                request.getGoalType(),
                request.getTitle(),
                request.getTargetValue(),
                request.getCurrentValue() != null ? request.getCurrentValue() : BigDecimal.ZERO,
                request.getUnit(),
                request.getDeadline(),
                request.getStatus() != null ? request.getStatus() : GoalStatus.ACTIVE
        );

        Goal saved = goalRepository.save(goal);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<GoalResponse> getUserGoals(User user, Pageable pageable) {
        return goalRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> getActiveGoals(User user) {
        return goalRepository.findByUserAndStatusOrderByDeadlineAsc(user, GoalStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GoalResponse getGoalForUser(Long id, User user) {
        Goal goal = goalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + id));
        return toResponse(goal);
    }

    @Transactional
    public GoalResponse updateGoal(Long id, User user, GoalRequest request) {
        validateGoalRequest(request);

        Goal goal = goalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + id));

        goal.setGoalType(request.getGoalType());
        goal.setTitle(request.getTitle());
        goal.setTargetValue(request.getTargetValue());
        goal.setCurrentValue(request.getCurrentValue() != null ? request.getCurrentValue() : BigDecimal.ZERO);
        goal.setUnit(request.getUnit());
        goal.setDeadline(request.getDeadline());

        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        } else {
            updateGoalStatusBasedOnProgress(goal);
        }

        Goal updated = goalRepository.save(goal);
        return toResponse(updated);
    }

    @Transactional
    public void deleteGoal(Long id, User user) {
        int deleted = goalRepository.deleteByIdAndUser(id, user);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Goal not found or access denied: " + id);
        }
    }

    @Transactional(readOnly = true)
    public long countUserGoals(User user) {
        return goalRepository.countByUser(user);
    }

    private void validateGoalRequest(GoalRequest request) {
        if (request.getGoalType() == null) {
            throw new IllegalArgumentException("Goal type is required");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.getTargetValue() == null || request.getTargetValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Target value must be non-negative");
        }
        if (request.getCurrentValue() != null && request.getCurrentValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Current value must not be negative");
        }
        if (request.getDeadline() == null) {
            throw new IllegalArgumentException("Deadline is required");
        }
    }

    private void updateGoalStatusBasedOnProgress(Goal goal) {
        if (goal.getStatus() == GoalStatus.PAUSED || goal.getStatus() == GoalStatus.CANCELLED) {
            return;
        }

        if (goal.getTargetValue() != null
                && goal.getTargetValue().compareTo(BigDecimal.ZERO) > 0
                && goal.getCurrentValue() != null
                && goal.getCurrentValue().compareTo(goal.getTargetValue()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED);
        } else if (goal.getStatus() == GoalStatus.COMPLETED) {
            goal.setStatus(GoalStatus.ACTIVE);
        }
    }

    private GoalResponse toResponse(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getGoalType(),
                goal.getTitle(),
                goal.getTargetValue(),
                goal.getCurrentValue(),
                goal.getUnit(),
                goal.getDeadline(),
                goal.getStatus(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }
}