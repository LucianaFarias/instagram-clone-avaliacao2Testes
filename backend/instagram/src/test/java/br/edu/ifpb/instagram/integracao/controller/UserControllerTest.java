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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifpb.instagram.model.entity.UserEntity;
import br.edu.ifpb.instagram.model.request.UserDetailsRequest;
import br.edu.ifpb.instagram.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnForbiddenWhenAccessingUsersWithoutAuth() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnForbiddenWhenAccessingUserByIdWithoutAuth() throws Exception {
        UserEntity user = new UserEntity();
        user.setFullName("Test User");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setEncryptedPassword(passwordEncoder.encode("password123"));
        UserEntity savedUser = userRepository.save(user);

        mockMvc.perform(get("/users/" + savedUser.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnForbiddenWhenUpdatingUserWithoutAuth() throws Exception {
        UserEntity user = new UserEntity();
        user.setFullName("Original User");
        user.setUsername("original");
        user.setEmail("original@example.com");
        user.setEncryptedPassword(passwordEncoder.encode("password123"));
        UserEntity savedUser = userRepository.save(user);

        UserDetailsRequest updateRequest = new UserDetailsRequest(
                savedUser.getId(),
                "updated@example.com",
                "newpassword",
                "Updated User",
                "updated"
        );

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnForbiddenWhenDeletingUserWithoutAuth() throws Exception {
        UserEntity user = new UserEntity();
        user.setFullName("Delete User");
        user.setUsername("deleteuser");
        user.setEmail("delete@example.com");
        user.setEncryptedPassword(passwordEncoder.encode("password123"));
        UserEntity savedUser = userRepository.save(user);

        mockMvc.perform(delete("/users/" + savedUser.getId()))
                .andExpect(status().isForbidden());
    }
}