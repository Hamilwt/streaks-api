package com.noww.streaks.controller;

import com.noww.streaks.dto.HabitRequest;
import com.noww.streaks.model.CheckIn;
import com.noww.streaks.model.Habit;
import com.noww.streaks.model.User;
import com.noww.streaks.service.StreaksService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/habits")
public class HabitController {

    private final StreaksService streaksService;

    public HabitController(StreaksService streaksService) {
        this.streaksService = streaksService;
    }

    @PostMapping
    public ResponseEntity<Habit> createHabit(@RequestBody HabitRequest request) {
        Habit habit = streaksService.createHabit(currentUser(), request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(habit);
    }

    @GetMapping
    public List<Habit> getHabits() {
        return streaksService.getHabits(currentUser());
    }

    @PostMapping("/{id}/check-ins")
    public ResponseEntity<CheckIn> checkIn(@PathVariable UUID id) {
        CheckIn checkIn = streaksService.checkIn(currentUser(), id);
        return ResponseEntity.status(HttpStatus.CREATED).body(checkIn);
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
