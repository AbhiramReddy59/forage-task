package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class DatabaseConduit {

    private final UserRepository userRepository;

    @Autowired
    public DatabaseConduit(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserRecord save(UserRecord user) {
        return userRepository.save(user);
    }

    public Optional<UserRecord> findById(Long id) {
        return userRepository.findById(id);
    }
}
