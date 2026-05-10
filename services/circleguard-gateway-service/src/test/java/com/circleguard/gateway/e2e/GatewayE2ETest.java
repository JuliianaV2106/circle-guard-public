package com.circleguard.gateway.e2e;

import com.circleguard.gateway.controller.GateController;
import com.circleguard.gateway.service.QrValidationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GateController.class)
public class GatewayE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QrValidationService validationService;

    // E2E Test 1: Flujo completo usuario sano entra al edificio
    @Test
    void shouldAllowHealthyUserToEnterBuilding() throws Exception {
        QrValidationService.ValidationResult result =
            new QrValidationService.ValidationResult(true, "GREEN", "Welcome");
        Mockito.when(validationService.validateToken("healthy-user-token"))
                .thenReturn(result);

        mockMvc.perform(post("/api/v1/gate/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\": \"healthy-user-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.status").value("GREEN"))
                .andExpect(jsonPath("$.message").value("Welcome"));
    }

    // E2E Test 2: Flujo completo usuario sospechoso es detenido
    @Test
    void shouldBlockSuspiciousUserAtGate() throws Exception {
        QrValidationService.ValidationResult result =
            new QrValidationService.ValidationResult(false, "RED", "Access denied - health risk");
        Mockito.when(validationService.validateToken("suspect-user-token"))
                .thenReturn(result);

        mockMvc.perform(post("/api/v1/gate/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\": \"suspect-user-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.status").value("RED"));
    }

    // E2E Test 3: Flujo completo usuario en seguimiento es redirigido
    @Test
    void shouldRedirectMonitoredUserToHealthCheck() throws Exception {
        QrValidationService.ValidationResult result =
            new QrValidationService.ValidationResult(false, "YELLOW", "Please proceed to health check");
        Mockito.when(validationService.validateToken("monitored-user-token"))
                .thenReturn(result);

        mockMvc.perform(post("/api/v1/gate/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\": \"monitored-user-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("YELLOW"))
                .andExpect(jsonPath("$.message").value("Please proceed to health check"));
    }

    // E2E Test 4: Flujo completo token expirado es rechazado
    @Test
    void shouldRejectExpiredToken() throws Exception {
        QrValidationService.ValidationResult result =
            new QrValidationService.ValidationResult(false, "RED", "Token expired");
        Mockito.when(validationService.validateToken("expired-token"))
                .thenReturn(result);

        mockMvc.perform(post("/api/v1/gate/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\": \"expired-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Token expired"));
    }

    // E2E Test 5: Flujo completo múltiples validaciones consecutivas
    @Test
    void shouldHandleMultipleConsecutiveValidations() throws Exception {
        QrValidationService.ValidationResult green =
            new QrValidationService.ValidationResult(true, "GREEN", "Welcome");
        QrValidationService.ValidationResult red =
            new QrValidationService.ValidationResult(false, "RED", "Denied");

        Mockito.when(validationService.validateToken("token-A")).thenReturn(green);
        Mockito.when(validationService.validateToken("token-B")).thenReturn(red);

        mockMvc.perform(post("/api/v1/gate/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\": \"token-A\"}"))
                .andExpect(jsonPath("$.status").value("GREEN"));

        mockMvc.perform(post("/api/v1/gate/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\": \"token-B\"}"))
                .andExpect(jsonPath("$.status").value("RED"));
    }
}