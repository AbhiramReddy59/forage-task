package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.kafka.TransactionListener;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class TaskTwoTests {


    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private FileLoader fileLoader;

    @Autowired
    private TransactionListener transactionListener;

    @Test
    void task_two_verifier() {
        transactionListener.clearReceivedTransactions();
        String[] transactionLines = fileLoader.loadStrings("/test_data/poiuytrewq.uiop");
        for (String transactionLine : transactionLines) {
            kafkaProducer.send(transactionLine);
        }

        Awaitility.await().atMost(Duration.ofSeconds(10)).until(() -> transactionListener.getReceivedTransactions().size() >= transactionLines.length);

        List<Transaction> received = transactionListener.getReceivedTransactions();
        assertEquals(transactionLines.length, received.size());

                System.out.println("Successfully received " + received.size() + " transactions.");
        System.out.println("Amounts of the first four transactions:");
        for (int i = 0; i < 4; i++) {
            if (i < received.size()) {
                System.out.println("Transaction " + (i + 1) + ": Amount = " + received.get(i).getAmount());
            }
        }
    }

}
