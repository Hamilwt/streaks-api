package com.noww.streaks.repository;

import com.noww.streaks.model.CheckIn;
import com.noww.streaks.model.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {

    boolean existsByHabitAndCheckInDate(Habit habit, LocalDate checkInDate);
}
