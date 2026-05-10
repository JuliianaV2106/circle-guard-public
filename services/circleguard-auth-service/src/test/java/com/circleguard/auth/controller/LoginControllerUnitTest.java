package com.circleguard.auth.controller;

import com.circleguard.auth.client.IdentityClient;
import com.circleguard.auth.service.JwtTokenService;
import com.circleguard.auth.service.CustomUserDetailsService;
import com.circleguard.auth.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@Import(SecurityConfig.class)
public class LoginControllerUnitTest {

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

    // Test 1: Login exitoso retorna token Bearer
    @Test
    void shouldReturnBearerTokenOnSuccessfulLogin() throws Exception {
        UUID anonymousId = UUID.randomUUID();
        String token = "valid-jwt-token";
        Authentication auth = Mockito.mock(Authentication.class);

        Mockito.when(authManager.authenticate(Mockito.any()))
                .thenReturn(auth);
        Mockito.when(identityClient.getAnonymousId(Mockito.any()))
                .thenReturn(anonymousId);
        Mockito.when(jwtService.generateToken(Mockito.any(), Mockito.any()))
                .thenReturn(token);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"user1\", \"password\": \"pass1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    // Test 2: Login con credenciales inválidas retorna 401
    @Test
    void shouldReturn401OnInvalidCredentials() throws Exception {
        Mockito.when(authManager.authenticate(Mockito.any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"wrong\", \"password\": \"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    // Test 3: Login sin body retorna 400
    @Test
    void shouldReturn400WhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    // Test 4: Token generado contiene anonymousId correcto
    @Test
    void shouldReturnCorrectAnonymousIdInResponse() throws Exception {
        UUID anonymousId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String token = "jwt-token-xyz";
        Authentication auth = Mockito.mock(Authentication.class);

        Mockito.when(authManager.authenticate(Mockito.any())).thenReturn(auth);
        Mockito.when(identityClient.getAnonymousId(Mockito.any())).thenReturn(anonymousId);
        Mockito.when(jwtService.generateToken(Mockito.any(), Mockito.any())).thenReturn(token);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"user2\", \"password\": \"pass2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonymousId")
                        .value("123e4567-e89b-12d3-a456-426614174000"));
    }

    // Test 5: Login con Content-Type incorrecto retorna 415
    @Test
    void shouldReturn415WhenContentTypeIsNotJson() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.TEXT_PLAIN)
                .content("username=user&password=pass"))
                .andExpect(status().isUnsupportedMediaType());
    }
}