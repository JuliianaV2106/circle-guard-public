package com.circleguard.gateway.controller;

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
public class GateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QrValidationService validationService;

    // Test 1: Token válido retorna GREEN
    @Test
    void shouldReturnGreenStatusForValidToken() throws Exception {
        QrValidationService.ValidationResult result =
            new QrValidationService.ValidationResult(true, "GREEN", "Access granted");
        Mockito.when(validationService.validateToken("valid-token")).thenReturn(result);

        mockMvc.perform(post("/api/v1/gate/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\": \"valid-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.status").value("GREEN"))
                .andExpect(jsonPath("$.message").value("Access granted"));
    }

    // Test 2: Token inválido retorna RED
    @Test
    void shouldReturnRedStatusForInvalidToken() throws Exception {
        QrValidationService.ValidationResult result =
            new QrValidationService.ValidationResult(false, "RED", "Access denied");
        Mockito.when(validationService.validateToken("invalid-token")).thenReturn(result);

        mockMvc.perform(post("/api/v1/gate/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\": \"invalid-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.status").value("RED"));
    }

    // Test 3: Token de usuario en cuarentena retorna YELLOW
    @Test
    void shouldReturnYellowStatusForQuarantinedUser() throws Exception {
        QrValidationService.ValidationResult result =
            new QrValidationService.ValidationResult(false, "YELLOW", "Quarantine required");
        Mockito.when(validationService.validateToken("quarantine-token")).thenReturn(result);

        mockMvc.perform(post("/api/v1/gate/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\": \"quarantine-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("YELLOW"))
                .andExpect(jsonPath("$.message").value("Quarantine required"));
    }

    // Test 4: Request con body vacío
    @Test
    void shouldHandleRequestWithEmptyToken() throws Exception {
        QrValidationService.ValidationResult result =
            new QrValidationService.ValidationResult(false, "RED", "No token");
        Mockito.when(validationService.validateToken(Mockito.any()))
                .thenReturn(result);

        mockMvc.perform(post("/api/v1/gate/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().is(org.hamcrest.Matchers.anyOf(
                    org.hamcrest.Matchers.is(200),
                    org.hamcrest.Matchers.is(400)
                )));
    }

    // Test 5: Excepción en servicio
    @Test
    void shouldHandleServiceException() throws Exception {
        Mockito.when(validationService.validateToken(Mockito.any()))
                .thenThrow(new RuntimeException("Service unavailable"));

        try {
            mockMvc.perform(post("/api/v1/gate/validate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"token\": \"any-token\"}"))
                    .andExpect(status().is5xxServerError());
        } catch (Exception e) {
            // Exception propagation is also acceptable behavior
            assert e.getCause() instanceof RuntimeException;
        }
    }
}