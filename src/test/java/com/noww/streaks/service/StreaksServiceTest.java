package com.noww.streaks.service;

import com.noww.streaks.exception.ConflictException;
import com.noww.streaks.exception.NotFoundException;
import com.noww.streaks.exception.UnprocessableEntityException;
import com.noww.streaks.model.Habit;
import com.noww.streaks.model.User;
import com.noww.streaks.repository.CheckInRepository;
import com.noww.streaks.repository.HabitRepository;
import com.noww.streaks.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreaksServiceTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private CheckInRepository checkInRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StreaksService streaksService;

    @Test
    @DisplayName("Checking in twice on the same day returns 409 Conflict")
    void checkIn_whenAlreadyCheckedInToday_returnsConflict() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("alice@example.com");
        user.setPassword("hashed-password");
        user.setBalance(0);

        Habit habit = new Habit();
        habit.setId(UUID.randomUUID());
        habit.setName("Morning run");
        habit.setUser(user);

        when(habitRepository.findByIdAndUser(habit.getId(), user)).thenReturn(Optional.of(habit));
        when(checkInRepository.existsByHabitAndCheckInDate(habit, LocalDate.now())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> streaksService.checkIn(user, habit.getId()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals(0, user.getBalance(), "balance must not change on a rejected check-in");
        verify(checkInRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Redeeming more than the balance returns 422 INSUFFICIENT_BALANCE")
    void redeem_whenAmountExceedsBalance_returnsUnprocessableEntity() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setBalance(10);

        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,
                () -> streaksService.redeem(user, 50));

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, exception.getStatus());
        assertEquals("INSUFFICIENT_BALANCE", exception.getError());
        assertEquals(10, user.getBalance(), "balance must not change on a rejected redeem");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Checking in to another user's habit returns 404 Not Found")
    void checkIn_whenHabitBelongsToAnotherUser_returnsNotFound() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setBalance(0);

        UUID habitId = UUID.randomUUID();

        // Ownership is enforced in the query itself, so a habit owned by
        // somebody else is indistinguishable from a habit that does not exist.
        when(habitRepository.findByIdAndUser(habitId, otherUser)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> streaksService.checkIn(otherUser, habitId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("HABIT_NOT_FOUND", exception.getError());
        assertEquals(0, otherUser.getBalance());
        verify(checkInRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }
}
