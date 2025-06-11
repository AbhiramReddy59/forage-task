package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionService.class);

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRepository;

    @Autowired
    public TransactionService(UserRepository userRepository,
                              TransactionRecordRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void processTransaction(Transaction tx) {
        Optional<UserRecord> senderOpt = userRepository.findById(tx.getSenderId());
        Optional<UserRecord> recipientOpt = userRepository.findById(tx.getRecipientId());

        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            LOG.warn("Invalid transaction, unknown user(s): {}", tx);
            return;
        }

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();

        if (sender.getBalance() < tx.getAmount()) {
            LOG.warn("Invalid transaction, insufficient funds: {}", tx);
            return;
        }

        // Adjust balances
        sender.setBalance(sender.getBalance() - tx.getAmount());
        recipient.setBalance(recipient.getBalance() + tx.getAmount());

        // Persist changes
        userRepository.save(sender);
        userRepository.save(recipient);

        transactionRepository.save(new TransactionRecord(sender, recipient, tx.getAmount()));
    }
}
