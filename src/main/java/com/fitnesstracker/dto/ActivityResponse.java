package com.fitnesstracker.dto;

import com.fitnesstracker.domain.enums.ActivityType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ActivityResponse {

    private Long id;
    private ActivityType activityType;
    private LocalDate activityDate;
    private Integer durationMinutes;
    private BigDecimal distanceKm;
    private Integer caloriesBurned;
    private String notes;
    private LocalDateTime createdAt;

    public ActivityResponse() {
    }

    public ActivityResponse(Long id, ActivityType activityType, LocalDate activityDate,
                            Integer durationMinutes, BigDecimal distanceKm, Integer caloriesBurned,
                            String notes, LocalDateTime createdAt) {
        this.id = id;
        this.activityType = activityType;
        this.activityDate = activityDate;
        this.durationMinutes = durationMinutes;
        this.distanceKm = distanceKm;
        this.caloriesBurned = caloriesBurned;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(LocalDate activityDate) {
        this.activityDate = activityDate;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Integer getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(Integer caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}