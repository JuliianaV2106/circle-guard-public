package com.circleguard.gateway.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Service
@EnableScheduling
public class BusinessMetricsService {

    private static final Logger log = LoggerFactory.getLogger(BusinessMetricsService.class);

    private final Counter qrCodesGenerated;
    private final Counter notificationsSent;
    private final Counter identityVerifications;
    private final Counter formSubmissions;
    private final Counter kafkaMessages;
    private final AtomicLong activeUsers = new AtomicLong(0);
    private final Random random = new Random();

    public BusinessMetricsService(MeterRegistry registry) {
        this.qrCodesGenerated = Counter.builder("circleguard_qr_codes_generated_total")
                .description("Total QR codes generated")
                .register(registry);
        this.notificationsSent = Counter.builder("circleguard_notifications_sent_total")
                .description("Total notifications sent")
                .register(registry);
        this.identityVerifications = Counter.builder("circleguard_identity_verifications_total")
                .description("Total identity verifications")
                .register(registry);
        this.formSubmissions = Counter.builder("circleguard_form_submissions_total")
                .description("Total form submissions")
                .register(registry);
        this.kafkaMessages = Counter.builder("circleguard_kafka_messages_total")
                .description("Total Kafka messages")
                .register(registry);
        registry.gauge("circleguard_active_users_total", activeUsers,
                AtomicLong::doubleValue);
    }

    @PostConstruct
    public void init() {
        activeUsers.set(42);
        log.info("Business metrics initialized");
    }

    @Scheduled(fixedRate = 3000)
    public void simulateQrGeneration() {
        qrCodesGenerated.increment(random.nextInt(3) + 1);
    }

    @Scheduled(fixedRate = 5000)
    public void simulateNotifications() {
        notificationsSent.increment(random.nextInt(5) + 2);
    }

    @Scheduled(fixedRate = 7000)
    public void simulateVerifications() {
        identityVerifications.increment(random.nextInt(2) + 1);
    }

    @Scheduled(fixedRate = 10000)
    public void simulateFormSubmissions() {
        formSubmissions.increment(random.nextInt(3));
    }

    @Scheduled(fixedRate = 8000)
    public void simulateKafkaMessages() {
        kafkaMessages.increment(random.nextInt(4) + 1);
    }

    @Scheduled(fixedRate = 15000)
    public void simulateActiveUsers() {
        long base = 42;
        long variation = random.nextInt(21) - 10;
        activeUsers.set(Math.max(10, base + variation));
    }
}
