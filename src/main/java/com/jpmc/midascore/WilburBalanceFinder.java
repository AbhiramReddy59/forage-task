package com.jpmc.midascore;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.service.TransactionService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

// Helper utility for manual debugging; NOT used by tests.
public class WilburBalanceFinder {
    public static void main(String[] args) throws Exception {
        // Reuse primary Spring Boot application context
        ConfigurableApplicationContext ctx = SpringApplication.run(MidasCoreApplication.class, args);

        TransactionService transactionService = ctx.getBean(TransactionService.class);
        UserRepository userRepository = ctx.getBean(UserRepository.class);

        String[] txLines = new String[]{}; // load your own data if needed
        Arrays.stream(txLines).forEach(l -> {
            String[] parts = l.split(", ");
            Transaction tx = new Transaction(Long.parseLong(parts[0]), Long.parseLong(parts[1]), Float.parseFloat(parts[2]));
            transactionService.processTransaction(tx);
        });

        UserRecord wilbur = userRepository.findById(9L).orElse(null);
        if (wilbur != null) {
            System.out.println("Wilbur balance=" + wilbur.getBalance());
        }
        ctx.close();
    }
}
