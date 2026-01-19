package br.edu.ifpb.instagram.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import br.edu.ifpb.instagram.model.entity.UserEntity;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private UserEntity user;

    @BeforeEach
    void setup() {

        userRepository.deleteAll();

        user = new UserEntity();
        user.setFullName("Rayane Rodrigues");
        user.setEmail("rayane@ifpb.edu.br");
        user.setUsername("rayane");
        user.setEncryptedPassword("123");
    }

    @Test
    void deveSalvarUsuario() {
        UserEntity salvo = userRepository.save(user);

        assertNotNull(salvo.getId());
        assertTrue(salvo.getId() > 0);

    }

    @Test
    void deveBuscarPorUsername() {
        userRepository.save(user);

        Optional<UserEntity> resultado =
                userRepository.findByUsername("rayane");

        assertTrue(resultado.isPresent());
    }

    @Test
    void deveVerificarExistenciaPorEmail() {
        userRepository.save(user);

        assertTrue(
                userRepository.existsByEmail("rayane@ifpb.edu.br"));
    }

    @Test
    void deveAtualizarParcialmenteUsuario() {
        UserEntity salvo = userRepository.save(user);

        int linhas = userRepository.updatePartialUser(
                "Novo Nome",
                null,
                null,
                null,
                salvo.getId()
        );

        assertEquals(1, linhas);

        UserEntity userAtualizado =
                userRepository.findById(salvo.getId())
                        .orElseThrow();

        assertEquals("Novo Nome", userAtualizado.getFullName());

    }

    @Test
    void deveRemoverUsuario() {
        UserEntity salvo = userRepository.save(user);
        userRepository.delete(salvo);
        assertFalse(userRepository.findById(salvo.getId()).isPresent());
    }
}
