package com.fitnesstracker.repository;

import com.fitnesstracker.domain.Activity;
import com.fitnesstracker.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByUserOrderByActivityDateDesc(User user);

    Page<Activity> findByUserOrderByActivityDateDesc(User user, Pageable pageable);

    Optional<Activity> findByIdAndUser(Long id, User user);

    long countByUser(User user);

    @Modifying
    @Query("DELETE FROM Activity a WHERE a.id = ?1 AND a.user = ?2")
    int deleteByIdAndUser(Long id, User user);

    List<Activity> findTop10ByUserOrderByActivityDateDesc(User user);
}