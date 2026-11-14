package com.noww.streaks.controller;

import com.noww.streaks.dto.RedeemRequest;
import com.noww.streaks.dto.WalletResponse;
import com.noww.streaks.model.User;
import com.noww.streaks.service.StreaksService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final StreaksService streaksService;

    public WalletController(StreaksService streaksService) {
        this.streaksService = streaksService;
    }

    @GetMapping
    public WalletResponse getBalance() {
        return new WalletResponse(streaksService.getWalletBalance(currentUser()));
    }

    @PostMapping("/redeem")
    public WalletResponse redeem(@RequestBody RedeemRequest request) {
        int balance = streaksService.redeem(currentUser(), request.getAmount());
        return new WalletResponse(balance);
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
