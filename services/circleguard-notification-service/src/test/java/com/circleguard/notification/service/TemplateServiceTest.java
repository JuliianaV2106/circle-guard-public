package com.circleguard.notification.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TemplateServiceTest {

    @Autowired
    private TemplateService templateService;

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

    @Test
    void testEmailTemplateGeneration() {
        String content = templateService.generateEmailContent("SUSPECT", "John Doe");
        assertThat(content).contains("John Doe");
        assertThat(content).contains("isolation guidelines");
        assertThat(content).contains("Testing Schedule");
    }

    @Test
    void testPushTemplateGeneration() {
        String content = templateService.generatePushContent("PROBABLE");
        assertThat(content).contains("Monitor symptoms");
    }

    @Test
    void testPushMetadataGeneration() {
        var metadata = templateService.generatePushMetadata("SUSPECT");
        assertThat(metadata).containsEntry("url", "circleguard://guidelines");
        var emptyMetadata = templateService.generatePushMetadata("OTHER");
        assertThat(emptyMetadata).isEmpty();
    }

    @Test
    void testSmsTemplateGeneration() {
        String content = templateService.generateSmsContent("SUSPECT");
        assertThat(content).contains("SUSPECT");
        assertThat(content).contains("check your email");
    }
}