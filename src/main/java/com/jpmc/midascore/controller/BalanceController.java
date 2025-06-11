package com.jpmc.midascore.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;

@RestController
public class BalanceController {

    private final UserRepository userRepository;

    @Autowired
    public BalanceController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestParam Long userId) {
        Optional<UserRecord> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            UserRecord u = userOpt.get();
            return ResponseEntity.ok(new com.jpmc.midascore.foundation.Balance(u.getBalance()));
        }
        return ResponseEntity.notFound().build();
    }
}
