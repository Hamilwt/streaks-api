package com.noww.streaks.service;

import com.noww.streaks.config.JwtService;
import com.noww.streaks.dto.LoginRequest;
import com.noww.streaks.dto.SignupRequest;
import com.noww.streaks.exception.ConflictException;
import com.noww.streaks.exception.UnauthorizedException;
import com.noww.streaks.model.User;
import com.noww.streaks.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("EMAIL_ALREADY_EXISTS",
                    "Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("INVALID_CREDENTIALS",
                        "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("INVALID_CREDENTIALS", "Invalid email or password");
        }

        return jwtService.generateToken(user);
    }
}
