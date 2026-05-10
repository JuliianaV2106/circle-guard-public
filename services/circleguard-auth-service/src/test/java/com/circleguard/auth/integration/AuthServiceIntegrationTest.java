package com.circleguard.auth.integration;

import com.circleguard.auth.client.IdentityClient;
import com.circleguard.auth.security.SecurityConfig;
import com.circleguard.auth.service.CustomUserDetailsService;
import com.circleguard.auth.service.JwtTokenService;
import com.circleguard.auth.controller.LoginController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@Import(SecurityConfig.class)
public class AuthServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authManager;

    @MockBean
    private JwtTokenService jwtService;

    @MockBean
    private IdentityClient identityClient;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    // Test 1: Flujo completo — auth-service obtiene anonymousId de identity-service
    @Test
    void shouldObtainAnonymousIdFromIdentityService() throws Exception {
        UUID anonymousId = UUID.randomUUID();
        String token = "integration-jwt-token";
        Authentication auth = Mockito.mock(Authentication.class);

        Mockito.when(authManager.authenticate(Mockito.any())).thenReturn(auth);
        Mockito.when(identityClient.getAnonymousId("integrationuser"))
               .thenReturn(anonymousId);
        Mockito.when(jwtService.generateToken(Mockito.eq(anonymousId), Mockito.any()))
               .thenReturn(token);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"integrationuser\", \"password\": \"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonymousId").value(anonymousId.toString()))
                .andExpect(jsonPath("$.token").value(token));
    }

    // Test 2: identity-service falla — auth-service propaga el error
    @Test
    void shouldPropagateErrorWhenIdentityServiceFails() throws Exception {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(authManager.authenticate(Mockito.any())).thenReturn(auth);
        Mockito.when(identityClient.getAnonymousId(Mockito.any()))
               .thenThrow(new RuntimeException("Identity service unavailable"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"user\", \"password\": \"pass\"}"))
                .andExpect(status().is5xxServerError());
    }

    // Test 3: Token generado incluye anonymousId del identity-service
    @Test
    void shouldIncludeIdentityServiceAnonymousIdInToken() throws Exception {
        UUID anonymousId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        Authentication auth = Mockito.mock(Authentication.class);

        Mockito.when(authManager.authenticate(Mockito.any())).thenReturn(auth);
        Mockito.when(identityClient.getAnonymousId(Mockito.any())).thenReturn(anonymousId);
        Mockito.when(jwtService.generateToken(Mockito.eq(anonymousId), Mockito.any()))
               .thenReturn("token-with-correct-id");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"user\", \"password\": \"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonymousId")
                        .value("550e8400-e29b-41d4-a716-446655440000"));
    }

    // Test 4: Dos usuarios distintos obtienen anonymousIds distintos
    @Test
    void shouldReturnDifferentAnonymousIdsForDifferentUsers() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Authentication auth = Mockito.mock(Authentication.class);

        // Usuario 1
        Mockito.when(authManager.authenticate(Mockito.any())).thenReturn(auth);
        Mockito.when(identityClient.getAnonymousId("user1")).thenReturn(id1);
        Mockito.when(jwtService.generateToken(Mockito.eq(id1), Mockito.any()))
               .thenReturn("token-user1");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"user1\", \"password\": \"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonymousId").value(id1.toString()));

        // Usuario 2
        Mockito.when(identityClient.getAnonymousId("user2")).thenReturn(id2);
        Mockito.when(jwtService.generateToken(Mockito.eq(id2), Mockito.any()))
               .thenReturn("token-user2");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"user2\", \"password\": \"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonymousId").value(id2.toString()));
    }

    // Test 5: Credenciales inválidas — identity-service nunca es llamado
    @Test
    void shouldNotCallIdentityServiceOnInvalidCredentials() throws Exception {
        Mockito.when(authManager.authenticate(Mockito.any()))
               .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"bad\", \"password\": \"wrong\"}"))
                .andExpect(status().isUnauthorized());

        // Verifica que identity-service NUNCA fue llamado
        Mockito.verify(identityClient, Mockito.never())
               .getAnonymousId(Mockito.any());
    }
}