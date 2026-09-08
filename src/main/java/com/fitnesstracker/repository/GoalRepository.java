package com.fitnesstracker.repository;

import com.fitnesstracker.domain.Goal;
import com.fitnesstracker.domain.User;
import com.fitnesstracker.domain.enums.GoalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUserOrderByCreatedAtDesc(User user);

    Page<Goal> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Optional<Goal> findByIdAndUser(Long id, User user);

    long countByUser(User user);

    @Modifying
    @Query("DELETE FROM Goal g WHERE g.id = ?1 AND g.user = ?2")
    int deleteByIdAndUser(Long id, User user);

    List<Goal> findByUserAndStatus(User user, GoalStatus status);

    List<Goal> findByUserAndStatusOrderByDeadlineAsc(User user, GoalStatus status);

    List<Goal> findByUserOrderByDeadlineAsc(User user);
}