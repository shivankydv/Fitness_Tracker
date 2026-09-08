package com.fitnesstracker.dto;

import com.fitnesstracker.domain.enums.ActivityType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ActivityRequest {

    @NotNull(message = "Activity type must not be null")
    private ActivityType activityType;

    @NotNull(message = "Activity date must not be null")
    private LocalDate activityDate;

    @NotNull(message = "Duration must not be null")
    @Min(value = 0, message = "Duration must not be negative")
    @Max(value = 1440, message = "Duration must not exceed 1440 minutes (24 hours)")
    private Integer durationMinutes;

    @NotNull(message = "Distance must not be null")
    @DecimalMin(value = "0.0", message = "Distance must not be negative")
    private BigDecimal distanceKm;

    @NotNull(message = "Calories burned must not be null")
    @Min(value = 0, message = "Calories burned must not be negative")
    @Max(value = 10000, message = "Calories burned must not exceed 10000")
    private Integer caloriesBurned;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    public ActivityRequest() {
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
}