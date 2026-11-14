package com.noww.streaks.service;

import com.noww.streaks.exception.BadRequestException;
import com.noww.streaks.exception.ConflictException;
import com.noww.streaks.exception.NotFoundException;
import com.noww.streaks.exception.UnprocessableEntityException;
import com.noww.streaks.model.CheckIn;
import com.noww.streaks.model.Habit;
import com.noww.streaks.model.User;
import com.noww.streaks.repository.CheckInRepository;
import com.noww.streaks.repository.HabitRepository;
import com.noww.streaks.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class StreaksService {

    private static final int CHECK_IN_REWARD = 10;

    private final HabitRepository habitRepository;
    private final CheckInRepository checkInRepository;
    private final UserRepository userRepository;

    public StreaksService(HabitRepository habitRepository,
                          CheckInRepository checkInRepository,
                          UserRepository userRepository) {
        this.habitRepository = habitRepository;
        this.checkInRepository = checkInRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Habit createHabit(User user, String name) {
        Habit habit = new Habit();
        habit.setName(name);
        habit.setUser(user);
        return habitRepository.save(habit);
    }

    @Transactional(readOnly = true)
    public List<Habit> getHabits(User user) {
        return habitRepository.findByUser(user);
    }

    @Transactional
    public CheckIn checkIn(User user, UUID habitId) {
        Habit habit = habitRepository.findByIdAndUser(habitId, user)
                .orElseThrow(() -> new NotFoundException("HABIT_NOT_FOUND", "Habit not found"));

        LocalDate today = LocalDate.now();
        if (checkInRepository.existsByHabitAndCheckInDate(habit, today)) {
            throw new ConflictException("ALREADY_CHECKED_IN",
                    "Habit has already been checked in today");
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setHabit(habit);
        checkIn.setCheckInDate(today);
        CheckIn saved = checkInRepository.save(checkIn);

        user.setBalance(user.getBalance() + CHECK_IN_REWARD);
        userRepository.save(user);

        return saved;
    }

    @Transactional(readOnly = true)
    public int getWalletBalance(User user) {
        return user.getBalance();
    }

    @Transactional
    public int redeem(User user, int amount) {
        if (amount <= 0) {
            throw new BadRequestException("INVALID_AMOUNT", "Amount must be greater than zero");
        }
        if (amount > user.getBalance()) {
            throw new UnprocessableEntityException("INSUFFICIENT_BALANCE", "Not enough coins");
        }

        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);
        return user.getBalance();
    }
}
