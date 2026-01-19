package br.edu.ifpb.instagram.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.entity.UserEntity;
import br.edu.ifpb.instagram.repository.UserRepository;

@SpringBootTest
public class UserServiceImplTest {

    @MockitoBean
    UserRepository userRepository; // Repositório simulado

    @Autowired
    UserServiceImpl userService; // Classe sob teste

    UserEntity mockUserEntity;

    // ==========================
    // Executa ANTES de cada teste

    @BeforeEach
    void setUp() {
        // Cria um usuário mockado (Entity) que será retornado pelo repositório
        mockUserEntity = new UserEntity();
        mockUserEntity.setId(1L);
        mockUserEntity.setFullName("Manu Azevedo");
        mockUserEntity.setEmail("manu@azevedo.dev");
        mockUserEntity.setUsername("manu_azevedo");
    }

    // ==========================
    // TESTE: Buscar usuário por ID (sucesso)
    @Test
    void testFindById_ReturnsUserDto() {
        // Configurar o comportamento do mock
        Long userId = 1L;

        UserEntity mockUserEntity = new UserEntity();
        mockUserEntity.setId(userId);
        mockUserEntity.setFullName("Paulo Pereira");
        mockUserEntity.setEmail("paulo@ppereira.dev");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUserEntity));

        // Executar o método a ser testado
        UserDto userDto = userService.findById(userId);

        // Verificar o resultado
        assertNotNull(userDto);
        assertEquals(mockUserEntity.getId(), userDto.id());
        assertEquals(mockUserEntity.getFullName(), userDto.fullName());
        assertEquals(mockUserEntity.getEmail(), userDto.email());

        // Verificar a interação com o mock
        verify(userRepository, times(1)).findById(userId);
    }

    // ==========================
    // TESTE: Buscar usuário por ID (usuário não encontrado)
    @Test
    void testFindById_ThrowsExceptionWhenUserNotFound() {
        // Configurar o comportamento do mock
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Executar e verificar a exceção
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.findById(userId);
        });

        assertEquals("User not found with id: 999", exception.getMessage());

        // Verificar a interação com o mock
        verify(userRepository, times(1)).findById(userId);
    }

    // ======== Meus Testes =======

    // ==========================
    // TESTE: Criar usuário
    @Test
    void testCreateUser_ReturnsUserDto() {
        // ARRANGE
        // Simula o comportamento do repositório ao salvar o usuário
        when(userRepository.save(any(UserEntity.class))).thenReturn(mockUserEntity);

        // DTO enviado para criação do usuário
        UserDto savedUser = new UserDto(null, "Manu Azevedo", "manu_azevedo", "manu@azevedo.dev", "123456", null);
        // -------- ACT --------
        UserDto userDto = userService.createUser(savedUser);

        // -------- ASSERT --------
        // Verifica se o DTO retornado não é nulo
        assertNotNull(userDto);
        // Verifica se os dados retornados estão corretos
        assertEquals(1L, userDto.id());
        assertEquals(mockUserEntity.getFullName(), userDto.fullName());
        assertEquals(mockUserEntity.getUsername(), userDto.username());
        assertEquals(mockUserEntity.getEmail(), userDto.email());
        assertEquals(mockUserEntity.getEncryptedPassword(), userDto.encryptedPassword());

        // Garante que o método save do repositório foi chamado exatamente uma vez
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    // ==========================
    // TESTE: Criar usuário com e-mail duplicado
    @Test
    void testCreateUser_WhenEmailAlreadyExists_ThrowsException() {

    // -------- ARRANGE --------
    // Simula que o e-mail já está cadastrado
    when(userRepository.existsByEmail("manu@azevedo.dev"))
            .thenReturn(true);

    UserDto newUser = new UserDto(
            null,
            "Manu Azevedo",
            "manu_azevedo",
            "manu@azevedo.dev",
            "123456",
            null
    );

    // -------- ACT + ASSERT --------
    // Verifica se a exceção é lançada ao tentar criar usuário duplicado
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        userService.createUser(newUser);
    });

    // Verifica a mensagem da exceção
    assertEquals("E-email already in use.", exception.getMessage());

    // Garante que o save NÃO foi chamado
    verify(userRepository, never()).save(any(UserEntity.class));
}

// ==========================
// TESTE: Criar usuário com username duplicado
@Test
void testCreateUser_WhenUsernameAlreadyExists_ThrowsException() {

    // -------- ARRANGE --------
    // Simula que já existe um usuário cadastrado
    when(userRepository.existsByUsername("manu_azevedo"))
            .thenReturn(true);
    
    // Cria um DTO com os dados do novo usuário
    UserDto newUser = new UserDto(
            null,
            "Manu Azevedo",
            "manu_azevedo",
            "manu@azevedo.dev",
            "123456",
            null
    );

    // -------- ACT + ASSERT --------
    // Executa o método createUser e verifica se
    // uma RuntimeException é lançada
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        userService.createUser(newUser);
    });
     // indicando que o username já está em uso
    assertEquals("Username already in use.", exception.getMessage());

    // Verifica que o save não foi chamado
    verify(userRepository, never()).save(any(UserEntity.class));
}

    // ==========================
    // TESTE: Deletar usuário existente
    @Test
    void testDeleteUser_WhenUserExists() {

        // ARRANGE
        // Simula que o usuário existe
        when(userRepository.existsById(1L)).thenReturn(true);

        // ACT
        // Executa o método de exclusão
        userService.deleteUser(1L);

        // ASSERT
        // Verifica se o delete foi chamado
        verify(userRepository).deleteById(1L);
    }

    // ==========================
// TESTE: Deletar usuário inexistente
@Test
void testDeleteUser_WhenUserDoesNotExist_ThrowsException() {

    // -------- ARRANGE --------
    // Simula que NÃO existe um usuário cadastrado
    when(userRepository.existsById(10L)).thenReturn(false);

    // -------- ACT + ASSERT --------
    // Executa o método deleteUser e verifica se
    // uma RuntimeException é lançada
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        userService.deleteUser(10L);
    });
    // indicando que o usuário não foi encontrado
    assertEquals("User not found with id: 10", exception.getMessage());

    // Verifica que delete NÃO foi chamado
    verify(userRepository, never()).deleteById(anyLong());
}

    // ==========================
    // TESTE: Atualizar usuário existente

    @Test
    void testUpdateUser_WhenUserExists_ReturnsUpdatedUserDto() {

        // -------- ARRANGE --------
        // Simula busca do usuário existente
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUserEntity));
        // Simula o save após atualização
        when(userRepository.save(any(UserEntity.class))).thenReturn(mockUserEntity);

        // DTO contendo os novos dados para atualização do usuário
        UserDto updatedDto = new UserDto(
                1L,
                "Manu ",
                "manu",
                "azevedo@gmail.com",
                null,
                null);

        // -------- ACT --------
        // Executa o método de atualização do usuário
        UserDto result = userService.updateUser(updatedDto);

        // -------- ASSERT --------
        // Verifica se os dados retornados foram atualizados corretamente
        assertEquals(updatedDto.fullName(), result.fullName());
        assertEquals(updatedDto.email(), result.email());

        // Verifica se o usuário foi buscado e salvo no repositório
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(UserEntity.class));
    }

    // ==========================
    // TESTE: Listar todos os usuários
    @Test
    void testFindAllUser_WhenUserExists_ReturnListUserDto() {

        // Cria uma lista contendo um usuário Entity mockado
        List<UserEntity> userEntities = List.of(mockUserEntity);

        // Configura o mock do repositório para retornar a lista criada
        when(userRepository.findAll()).thenReturn(userEntities);

        // -------- ACT --------
        // Executa o método do service que lista todos os usuários
        List<UserDto> result = userService.findAll();

        // -------- ASSERT --------
        assertNotNull(result);
        // Verifica se a lista contém exatamente um usuário
        assertEquals(1, result.size());

        UserDto userDto = result.get(0);
        // Verifica se os dados do usuário foram corretamente mapeados
        assertEquals(1L, userDto.id());
        assertEquals("Manu Azevedo", userDto.fullName());
        assertEquals("manu_azevedo", userDto.username());
        assertEquals("manu@azevedo.dev", userDto.email());

        // Verifica se o método findAll() do repositório
        verify(userRepository, times(1)).findAll();
    }

}
