package com.fitnesstracker.repository;

import com.fitnesstracker.domain.Goal;
import com.fitnesstracker.domain.User;
import com.fitnesstracker.domain.enums.GoalStatus;
import com.fitnesstracker.domain.enums.GoalType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GoalRepositoryTest {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User createUser(String name, String email) {
        User user = new User(name, email, "password123");
        return userRepository.save(user);
    }

    private Goal createGoal(User user, GoalType type, String title, BigDecimal target,
                            BigDecimal current, String unit, LocalDate deadline, GoalStatus status) {
        Goal goal = new Goal(user, type, title, target, current, unit, deadline, status);
        return goalRepository.save(goal);
    }

    @Test
    void findByUserOrderByCreatedAtDesc_returnsOnlyOwnedGoals() {
        User userA = createUser("User A", "usera@example.com");
        User userB = createUser("User B", "userb@example.com");

        createGoal(userA, GoalType.DISTANCE, "Run 100km", new BigDecimal("100"), BigDecimal.ZERO, "km",
                LocalDate.now().plusDays(30), GoalStatus.ACTIVE);
        createGoal(userA, GoalType.WEIGHT, "Lose 5kg", new BigDecimal("5"), BigDecimal.ZERO, "kg",
                LocalDate.now().plusDays(60), GoalStatus.ACTIVE);
        createGoal(userB, GoalType.CALORIES, "Burn 5000 cal", new BigDecimal("5000"), BigDecimal.ZERO, "cal",
                LocalDate.now().plusDays(30), GoalStatus.ACTIVE);

        List<Goal> userAGoals = goalRepository.findByUserOrderByCreatedAtDesc(userA);

        assertThat(userAGoals).hasSize(2);
        assertThat(userAGoals).allMatch(g -> g.getUser().getId().equals(userA.getId()));
    }

    @Test
    void findByIdAndUser_whenGoalBelongsToUser_returnsGoal() {
        User user = createUser("User", "user@example.com");
        Goal goal = createGoal(user, GoalType.DISTANCE, "Run 100km", new BigDecimal("100"),
                BigDecimal.ZERO, "km", LocalDate.now().plusDays(30), GoalStatus.ACTIVE);

        Optional<Goal> found = goalRepository.findByIdAndUser(goal.getId(), user);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(goal.getId());
    }

    @Test
    void findByIdAndUser_whenGoalBelongsToOtherUser_returnsEmpty() {
        User userA = createUser("User A", "usera@example.com");
        User userB = createUser("User B", "userb@example.com");
        Goal goal = createGoal(userA, GoalType.DISTANCE, "Run 100km", new BigDecimal("100"),
                BigDecimal.ZERO, "km", LocalDate.now().plusDays(30), GoalStatus.ACTIVE);

        Optional<Goal> found = goalRepository.findByIdAndUser(goal.getId(), userB);

        assertThat(found).isEmpty();
    }

    @Test
    void deleteByIdAndUser_whenGoalBelongsToUser_deletesGoal() {
        User user = createUser("User", "user@example.com");
        Goal goal = createGoal(user, GoalType.DISTANCE, "Run 100km", new BigDecimal("100"),
                BigDecimal.ZERO, "km", LocalDate.now().plusDays(30), GoalStatus.ACTIVE);

        int deleted = goalRepository.deleteByIdAndUser(goal.getId(), user);

        assertThat(deleted).isEqualTo(1);
        entityManager.flush();
        entityManager.clear();
        assertThat(goalRepository.findById(goal.getId())).isEmpty();
    }

    @Test
    void deleteByIdAndUser_whenGoalBelongsToOtherUser_doesNotDelete() {
        User userA = createUser("User A", "usera@example.com");
        User userB = createUser("User B", "userb@example.com");
        Goal goal = createGoal(userA, GoalType.DISTANCE, "Run 100km", new BigDecimal("100"),
                BigDecimal.ZERO, "km", LocalDate.now().plusDays(30), GoalStatus.ACTIVE);

        int deleted = goalRepository.deleteByIdAndUser(goal.getId(), userB);

        assertThat(deleted).isEqualTo(0);
        assertThat(goalRepository.findById(goal.getId())).isPresent();
    }

    @Test
    void countByUser_returnsCorrectCount() {
        User userA = createUser("User A", "usera@example.com");
        User userB = createUser("User B", "userb@example.com");

        createGoal(userA, GoalType.DISTANCE, "Run 100km", new BigDecimal("100"), BigDecimal.ZERO, "km",
                LocalDate.now().plusDays(30), GoalStatus.ACTIVE);
        createGoal(userA, GoalType.WEIGHT, "Lose 5kg", new BigDecimal("5"), BigDecimal.ZERO, "kg",
                LocalDate.now().plusDays(60), GoalStatus.ACTIVE);
        createGoal(userB, GoalType.CALORIES, "Burn 5000 cal", new BigDecimal("5000"), BigDecimal.ZERO, "cal",
                LocalDate.now().plusDays(30), GoalStatus.ACTIVE);

        long countA = goalRepository.countByUser(userA);
        long countB = goalRepository.countByUser(userB);

        assertThat(countA).isEqualTo(2);
        assertThat(countB).isEqualTo(1);
    }

    @Test
    void findByUserAndStatus_returnsGoalsWithMatchingStatus() {
        User user = createUser("User", "user@example.com");

        createGoal(user, GoalType.DISTANCE, "Active 1", new BigDecimal("100"), BigDecimal.ZERO, "km",
                LocalDate.now().plusDays(30), GoalStatus.ACTIVE);
        createGoal(user, GoalType.DISTANCE, "Active 2", new BigDecimal("200"), BigDecimal.ZERO, "km",
                LocalDate.now().plusDays(60), GoalStatus.ACTIVE);
        createGoal(user, GoalType.WEIGHT, "Completed", new BigDecimal("5"), BigDecimal.ZERO, "kg",
                LocalDate.now().plusDays(30), GoalStatus.COMPLETED);
        createGoal(user, GoalType.CALORIES, "Paused", new BigDecimal("5000"), BigDecimal.ZERO, "cal",
                LocalDate.now().plusDays(30), GoalStatus.PAUSED);

        List<Goal> activeGoals = goalRepository.findByUserAndStatus(user, GoalStatus.ACTIVE);
        List<Goal> completedGoals = goalRepository.findByUserAndStatus(user, GoalStatus.COMPLETED);

        assertThat(activeGoals).hasSize(2);
        assertThat(completedGoals).hasSize(1);
        assertThat(activeGoals).allMatch(g -> g.getStatus() == GoalStatus.ACTIVE);
    }

    @Test
    void findByUserAndStatusOrderByDeadlineAsc_ordersByDeadline() {
        User user = createUser("User", "user@example.com");

        createGoal(user, GoalType.DISTANCE, "Later", new BigDecimal("100"), BigDecimal.ZERO, "km",
                LocalDate.now().plusDays(30), GoalStatus.ACTIVE);
        createGoal(user, GoalType.DISTANCE, "Sooner", new BigDecimal("200"), BigDecimal.ZERO, "km",
                LocalDate.now().plusDays(10), GoalStatus.ACTIVE);

        List<Goal> goals = goalRepository.findByUserAndStatusOrderByDeadlineAsc(user, GoalStatus.ACTIVE);

        assertThat(goals).hasSize(2);
        assertThat(goals.get(0).getDeadline()).isBefore(goals.get(1).getDeadline());
    }

    @Test
    void findByUserOrderByDeadlineAsc_ordersAllGoalsByDeadline() {
        User user = createUser("User", "user@example.com");

        createGoal(user, GoalType.DISTANCE, "Goal 1", new BigDecimal("100"), BigDecimal.ZERO, "km",
                LocalDate.now().plusDays(30), GoalStatus.ACTIVE);
        createGoal(user, GoalType.WEIGHT, "Goal 2", new BigDecimal("5"), BigDecimal.ZERO, "kg",
                LocalDate.now().plusDays(10), GoalStatus.ACTIVE);
        createGoal(user, GoalType.CALORIES, "Goal 3", new BigDecimal("5000"), BigDecimal.ZERO, "cal",
                LocalDate.now().plusDays(60), GoalStatus.COMPLETED);

        List<Goal> goals = goalRepository.findByUserOrderByDeadlineAsc(user);

        assertThat(goals).hasSize(3);
        assertThat(goals.get(0).getDeadline()).isBefore(goals.get(1).getDeadline());
        assertThat(goals.get(1).getDeadline()).isBefore(goals.get(2).getDeadline());
    }

    @Test
    void findByUserOrderByCreatedAtDesc_withPagination_returnsPage() {
        User user = createUser("User", "user@example.com");
        for (int i = 0; i < 5; i++) {
            createGoal(user, GoalType.DISTANCE, "Goal " + i, new BigDecimal("100"), BigDecimal.ZERO, "km",
                    LocalDate.now().plusDays(30), GoalStatus.ACTIVE);
        }

        Pageable pageable = PageRequest.of(0, 3);
        Page<Goal> page = goalRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }
}