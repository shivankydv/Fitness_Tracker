package com.fitnesstracker.repository;

import com.fitnesstracker.domain.Activity;
import com.fitnesstracker.domain.User;
import com.fitnesstracker.domain.enums.ActivityType;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ActivityRepositoryTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User createUser(String name, String email) {
        User user = new User(name, email, "password123");
        return userRepository.save(user);
    }

    private Activity createActivity(User user, ActivityType type, LocalDate date,
                                    Integer duration, BigDecimal distance, Integer calories, String notes) {
        Activity activity = new Activity(user, type, date, duration, distance, calories, notes);
        return activityRepository.save(activity);
    }

    @Test
    void findByUserOrderByActivityDateDesc_returnsOnlyOwnedActivities() {
        User userA = createUser("User A", "usera@example.com");
        User userB = createUser("User B", "userb@example.com");

        createActivity(userA, ActivityType.RUNNING, LocalDate.now(), 30, new BigDecimal("5.0"), 300, "Run A");
        createActivity(userA, ActivityType.WALKING, LocalDate.now().minusDays(1), 60, new BigDecimal("4.0"), 200, "Walk A");
        createActivity(userB, ActivityType.CYCLING, LocalDate.now(), 45, new BigDecimal("20.0"), 400, "Cycle B");

        List<Activity> userAActivities = activityRepository.findByUserOrderByActivityDateDesc(userA);

        assertThat(userAActivities).hasSize(2);
        assertThat(userAActivities).allMatch(a -> a.getUser().getId().equals(userA.getId()));
        assertThat(userAActivities.get(0).getActivityDate()).isAfterOrEqualTo(userAActivities.get(1).getActivityDate());
    }

    @Test
    void findByIdAndUser_whenActivityBelongsToUser_returnsActivity() {
        User user = createUser("User", "user@example.com");
        Activity activity = createActivity(user, ActivityType.RUNNING, LocalDate.now(), 30, new BigDecimal("5.0"), 300, "Run");

        Optional<Activity> found = activityRepository.findByIdAndUser(activity.getId(), user);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(activity.getId());
    }

    @Test
    void findByIdAndUser_whenActivityBelongsToOtherUser_returnsEmpty() {
        User userA = createUser("User A", "usera@example.com");
        User userB = createUser("User B", "userb@example.com");
        Activity activity = createActivity(userA, ActivityType.RUNNING, LocalDate.now(), 30, new BigDecimal("5.0"), 300, "Run");

        Optional<Activity> found = activityRepository.findByIdAndUser(activity.getId(), userB);

        assertThat(found).isEmpty();
    }

    @Test
    void deleteByIdAndUser_whenActivityBelongsToUser_deletesActivity() {
        User user = createUser("User", "user@example.com");
        Activity activity = createActivity(user, ActivityType.RUNNING, LocalDate.now(), 30, new BigDecimal("5.0"), 300, "Run");

        int deleted = activityRepository.deleteByIdAndUser(activity.getId(), user);

        assertThat(deleted).isEqualTo(1);
        entityManager.flush();
        entityManager.clear();
        assertThat(activityRepository.findById(activity.getId())).isEmpty();
    }

    @Test
    void deleteByIdAndUser_whenActivityBelongsToOtherUser_doesNotDelete() {
        User userA = createUser("User A", "usera@example.com");
        User userB = createUser("User B", "userb@example.com");
        Activity activity = createActivity(userA, ActivityType.RUNNING, LocalDate.now(), 30, new BigDecimal("5.0"), 300, "Run");

        int deleted = activityRepository.deleteByIdAndUser(activity.getId(), userB);

        assertThat(deleted).isEqualTo(0);
        assertThat(activityRepository.findById(activity.getId())).isPresent();
    }

    @Test
    void countByUser_returnsCorrectCount() {
        User userA = createUser("User A", "usera@example.com");
        User userB = createUser("User B", "userb@example.com");

        createActivity(userA, ActivityType.RUNNING, LocalDate.now(), 30, new BigDecimal("5.0"), 300, "Run 1");
        createActivity(userA, ActivityType.WALKING, LocalDate.now(), 60, new BigDecimal("4.0"), 200, "Walk 1");
        createActivity(userB, ActivityType.CYCLING, LocalDate.now(), 45, new BigDecimal("20.0"), 400, "Cycle 1");

        long countA = activityRepository.countByUser(userA);
        long countB = activityRepository.countByUser(userB);

        assertThat(countA).isEqualTo(2);
        assertThat(countB).isEqualTo(1);
    }

    @Test
    void findByUserOrderByActivityDateDesc_withPagination_returnsPage() {
        User user = createUser("User", "user@example.com");
        for (int i = 0; i < 5; i++) {
            createActivity(user, ActivityType.RUNNING, LocalDate.now().minusDays(i), 30, new BigDecimal("5.0"), 300, "Run " + i);
        }

        Pageable pageable = PageRequest.of(0, 3);
        Page<Activity> page = activityRepository.findByUserOrderByActivityDateDesc(user, pageable);

        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void findTop10ByUserOrderByActivityDateDesc_returnsLimitedResults() {
        User user = createUser("User", "user@example.com");
        for (int i = 0; i < 15; i++) {
            createActivity(user, ActivityType.RUNNING, LocalDate.now().minusDays(i), 30, new BigDecimal("5.0"), 300, "Run " + i);
        }

        List<Activity> recent = activityRepository.findTop10ByUserOrderByActivityDateDesc(user);

        assertThat(recent).hasSize(10);
    }
}