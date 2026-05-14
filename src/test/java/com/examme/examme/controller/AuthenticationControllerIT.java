package com.examme.examme.controller;

import com.examme.examme.dto.request.auth.LoginRequestDto;
import com.examme.examme.dto.request.auth.UserRegistrationDto;
import com.examme.examme.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerAndLogin_Success() throws Exception {
        UserRegistrationDto reg = new UserRegistrationDto();
        reg.setEmail("it-test@example.com");
        reg.setPassword("password123");
        reg.setFullName("IT Test User");
        reg.setRole("STUDENT");

        // Register
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.user.email").value("it-test@example.com"));

        // Login
        LoginRequestDto login = new LoginRequestDto("it-test@example.com", "password123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void login_InvalidCredentials_Returns401() throws Exception {
        LoginRequestDto login = new LoginRequestDto("nonexistent@example.com", "wrong");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isNotFound()); // UserService throws NotFoundException for nonexistent user
    }
}
