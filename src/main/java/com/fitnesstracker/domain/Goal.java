package com.fitnesstracker.domain;

import com.fitnesstracker.domain.enums.GoalStatus;
import com.fitnesstracker.domain.enums.GoalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User user;

    @NotNull(message = "Goal type must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 30)
    private GoalType goalType;

    @NotNull(message = "Title must not be blank")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotNull(message = "Target value must not be null")
    @DecimalMin(value = "0.0", message = "Target value must not be negative")
    @Column(name = "target_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal targetValue;

    @NotNull(message = "Current value must not be null")
    @DecimalMin(value = "0.0", message = "Current value must not be negative")
    @Column(name = "current_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentValue;

    @Size(max = 50, message = "Unit must not exceed 50 characters")
    @Column(name = "unit", length = 50)
    private String unit;

    @NotNull(message = "Deadline must not be null")
    @Column(name = "deadline", nullable = false)
    private LocalDate deadline;

    @NotNull(message = "Status must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GoalStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Goal() {
    }

    public Goal(User user, GoalType goalType, String title, BigDecimal targetValue,
                BigDecimal currentValue, String unit, LocalDate deadline, GoalStatus status) {
        this.user = user;
        this.goalType = goalType;
        this.title = title;
        this.targetValue = targetValue;
        this.currentValue = currentValue;
        this.unit = unit;
        this.deadline = deadline;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = GoalStatus.ACTIVE;
        }
        if (this.currentValue == null) {
            this.currentValue = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    @Override
    public String toString() {
        return "Goal{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", goalType=" + goalType +
                ", title='" + title + '\'' +
                ", targetValue=" + targetValue +
                ", currentValue=" + currentValue +
                ", unit='" + unit + '\'' +
                ", deadline=" + deadline +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}