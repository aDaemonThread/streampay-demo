/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.demo.streampay.simulation.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import io.aklivity.zilla.demo.streampay.data.model.Transaction;

@Service
public class SimulatePayment
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SimulatePayment.class);

    @Value("${transactions.topic:transactions}")
    String transactionsTopic;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private SimulateUser simulateUser;
    @Autowired
    private Random random;

    public void makePaymentForVirtualUser()
    {
        final Transaction transaction = creatPaymentForVirtualUser();
        if (transaction != null)
        {
            kafkaTemplate.send(transactionsTopic, transaction.getOwnerId(), transaction);
        }
    }

    public void makePaymentForRealUser()
    {
        final Transaction transaction = creatPaymentForRealUser();
        if (transaction != null)
        {
            kafkaTemplate.send(transactionsTopic, transaction.getOwnerId(), transaction);
        }
    }

    private Transaction creatPaymentForVirtualUser()
    {
        final String ownerId = simulateUser.randomVirtualUser().getId();
        final String userId = simulateUser.randomVirtualUser().getId();
        final double amount = new BigDecimal(random.nextDouble(1, 200))
            .setScale(2, RoundingMode.HALF_DOWN).doubleValue();

        Transaction transaction = null;
        if (ownerId != userId)
        {
            transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .ownerId(ownerId)
                .userId(userId)
                .amount(amount)
                .timestamp(Instant.now().toEpochMilli())
                .build();

            LOGGER.info("Payment made from {} to {}", ownerId, userId);
        }

        return transaction;
    }

    private Transaction creatPaymentForRealUser()
    {
        final String ownerId = simulateUser.randomRealUser().getId();
        final String userId = simulateUser.randomVirtualUser().getId();
        final double amount = new BigDecimal(random.nextDouble(1, 200))
            .setScale(2, RoundingMode.HALF_DOWN).doubleValue();

        Transaction transaction = null;
        if (ownerId != null)
        {
            transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .ownerId(ownerId)
                .userId(userId)
                .amount(amount)
                .timestamp(Instant.now().toEpochMilli())
                .build();

            LOGGER.info("Payment made from {} to {}", ownerId, userId);
        }

        return transaction;
    }
}
