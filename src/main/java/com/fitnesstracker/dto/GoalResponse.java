package com.fitnesstracker.dto;

import com.fitnesstracker.domain.enums.GoalStatus;
import com.fitnesstracker.domain.enums.GoalType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class GoalResponse {

    private Long id;
    private GoalType goalType;
    private String title;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private String unit;
    private LocalDate deadline;
    private GoalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GoalResponse() {
    }

    public GoalResponse(Long id, GoalType goalType, String title, BigDecimal targetValue,
                        BigDecimal currentValue, String unit, LocalDate deadline, GoalStatus status,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.goalType = goalType;
        this.title = title;
        this.targetValue = targetValue;
        this.currentValue = currentValue;
        this.unit = unit;
        this.deadline = deadline;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public void setGoalType(GoalType goalType) {
        this.goalType = goalType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(BigDecimal targetValue) {
        this.targetValue = targetValue;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getProgressPercentage() {
        if (targetValue == null || targetValue.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        BigDecimal percentage = currentValue.divide(targetValue, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
        return Math.min(100.0, percentage.doubleValue());
    }
}