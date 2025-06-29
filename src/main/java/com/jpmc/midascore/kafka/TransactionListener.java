package com.jpmc.midascore.kafka;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class TransactionListener {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionListener.class);

    private final List<Transaction> receivedTransactions = Collections.synchronizedList(new ArrayList<>());

    private final TransactionService transactionService;

    public TransactionListener(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(Transaction transaction) {
        LOG.info("Received transaction: Amount = {}", transaction.getAmount());
        receivedTransactions.add(transaction);
        transactionService.processTransaction(transaction);
    }

    public List<Transaction> getReceivedTransactions() {
        return receivedTransactions;
    }

    public void clearReceivedTransactions() {
        receivedTransactions.clear();
    }
}
