package com.circleguard.notification.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import java.util.concurrent.CompletableFuture;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RoomReservationServiceTest {

    @Autowired
    private RoomReservationService roomReservationService;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;
    @MockBean
    private NotificationDispatcher dispatcher;
    @MockBean
    private org.springframework.mail.javamail.JavaMailSender mailSender;
    @MockBean
    private org.springframework.web.reactive.function.client.WebClient.Builder webClientBuilder;
    @MockBean
    private EmailService emailService;
    @MockBean
    private SmsService smsService;
    @MockBean
    private PushService pushService;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    void testCancelReservation() {
        CompletableFuture<Void> future = roomReservationService.cancelReservation("circle-1", "loc-1");
        future.join();
        assertThat(future).isCompleted();
    }
}