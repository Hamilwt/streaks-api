package com.noww.streaks.repository;

import com.noww.streaks.model.Habit;
import com.noww.streaks.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HabitRepository extends JpaRepository<Habit, UUID> {

    List<Habit> findByUser(User user);

    Optional<Habit> findByIdAndUser(UUID id, User user);
}
