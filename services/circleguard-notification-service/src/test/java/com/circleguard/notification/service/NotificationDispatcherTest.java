package com.circleguard.notification.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import java.util.concurrent.CompletableFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class NotificationDispatcherTest {

    @Autowired
    private NotificationDispatcher dispatcher;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;
    @MockBean
    private org.springframework.mail.javamail.JavaMailSender mailSender;
    @MockBean
    private org.springframework.web.reactive.function.client.WebClient.Builder webClientBuilder;
    @MockBean
    private EmailService emailService;
    @MockBean
    private SmsService smsService;
    @MockBean
    private TemplateService templateService;
    @MockBean
    private PushService pushService;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    void shouldDispatchToAllChannelsConcurrently() throws Exception {
        when(emailService.sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(null));
        when(smsService.sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(null));
        when(pushService.sendAsync(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));
        dispatcher.dispatch("user-123", "Your health status has changed.");
        verify(emailService, timeout(1000)).sendAsync(eq("user-123"), any());
        verify(smsService, timeout(1000)).sendAsync(eq("user-123"), any());
        verify(pushService, timeout(1000)).sendAsync(eq("user-123"), any(), any());
    }
}