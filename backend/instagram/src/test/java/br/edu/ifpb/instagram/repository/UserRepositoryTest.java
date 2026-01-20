package br.edu.ifpb.instagram.repository;

import br.edu.ifpb.instagram.model.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private UserEntity user;

    @BeforeEach
    void setup() {
        // Limpa a tabela antes de cada teste
        userRepository.deleteAll();

        // Cria usuário base para os testes
        user = new UserEntity();
        user.setFullName("Rayane Rodrigues");
        user.setEmail("rayane@ifpb.edu.br");
        user.setUsername("rayane");
        user.setEncryptedPassword("123");
    }

    @Test
    void deveSalvarUsuario() {
        UserEntity salvo = userRepository.save(user);

        //assertNotNull(salvo.getId());
        assertTrue(salvo.getId() > 0);
    }

    @Test
    void deveBuscarPorUsername() {
        userRepository.save(user);

        Optional<UserEntity> resultado = userRepository.findByUsername("rayane");

        assertTrue(resultado.isPresent());
    }

    @Test
    void deveVerificarExistenciaPorEmail() {
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("rayane@ifpb.edu.br"));
    }

    @Test
    void deveAtualizarParcialmenteUsuario() {
        // Salva usuário
        UserEntity salvo = userRepository.save(user);

        // Tenta atualizar parcialmente o fullName
        int linhas = userRepository.updatePartialUser(
                "Novo Nome",  // queremos que isso mude
                null,
                null,
                null,
                salvo.getId()
        );

        assertEquals(1, linhas, "Deve atualizar 1 linha");

        // Busca o usuário atualizado
        UserEntity userAtualizado = userRepository.findById(salvo.getId())
                .orElseThrow();

        // Aqui o teste **vai falhar**, mostrando o bug
        assertEquals("Novo Nome", userAtualizado.getFullName(),
                "O fullName deveria ter sido atualizado para 'Novo Nome'");
    }

    @Test
    void deveRemoverUsuario() {
        UserEntity salvo = userRepository.save(user);
        userRepository.delete(salvo);
        assertFalse(userRepository.findById(salvo.getId()).isPresent());
    }
}