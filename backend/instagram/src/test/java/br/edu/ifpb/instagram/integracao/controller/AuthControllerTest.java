package br.edu.ifpb.instagram.integracao.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifpb.instagram.model.entity.UserEntity;
import br.edu.ifpb.instagram.model.request.LoginRequest;
import br.edu.ifpb.instagram.model.request.UserDetailsRequest;
import br.edu.ifpb.instagram.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldSignUpUserSuccessfully() throws Exception {

        UserDetailsRequest request = new UserDetailsRequest(
                null,
                "maria@email.com",
                "123456",
                "Maria Silva",
                "maria"
        );

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.fullName").value("Maria Silva"))
                .andExpect(jsonPath("$.username").value("maria"))
                .andExpect(jsonPath("$.email").value("maria@email.com"));
    }

    @Test
    void shouldSignInSuccessfully() throws Exception {

        UserEntity user = new UserEntity();
        user.setFullName("João Silva");
        user.setUsername("joao");
        user.setEmail("joao@email.com");

        if (passwordEncoder != null) {
            user.setEncryptedPassword(passwordEncoder.encode("123456"));
        } else {
            user.setEncryptedPassword("123456");
        }

        userRepository.save(user);

        LoginRequest request = new LoginRequest(
                "joao",
                "123456"
        );

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("joao"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldReturnConflictWhenSignUpWithDuplicateEmail() throws Exception {
        // Primeiro cadastro
        UserDetailsRequest firstRequest = new UserDetailsRequest(
                null,
                "teste@email.com",
                "123456",
                "Primeiro Usuario",
                "primeiro"
        );

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Segundo cadastro com mesmo email
        UserDetailsRequest secondRequest = new UserDetailsRequest(
                null,
                "teste@email.com", // Email duplicado
                "654321",
                "Segundo Usuario",
                "segundo"
        );

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("E-email already in use."));
    }

    @Test
    void shouldReturnUnauthorizedWhenSignInWithInvalidCredentials() throws Exception {
        // Criar usuário válido
        UserEntity user = new UserEntity();
        user.setFullName("Teste Usuario");
        user.setUsername("teste");
        user.setEmail("teste@email.com");

        if (passwordEncoder != null) {
            user.setEncryptedPassword(passwordEncoder.encode("senhaCorreta"));
        } else {
            user.setEncryptedPassword("senhaCorreta");
        }

        userRepository.save(user);

        // Tentar login com senha incorreta
        LoginRequest request = new LoginRequest(
                "teste",
                "senhaIncorreta"
        );

        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}

