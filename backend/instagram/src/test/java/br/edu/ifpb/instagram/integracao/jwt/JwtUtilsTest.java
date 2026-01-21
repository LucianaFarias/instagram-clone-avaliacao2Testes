

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import br.edu.ifpb.instagram.security.JwtUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    private final String USUARIO_TESTE = "usuario_exemplo";

    @BeforeEach
    void setUp() {
       
        MockitoAnnotations.openMocks(this);
        jwtUtils = new JwtUtils();
    }

    @Test
    @DisplayName("Deve gerar um token JWT válido quando receber uma autenticação")
    void deveGerarTokenComSucesso() {
        
        when(authentication.getName()).thenReturn(USUARIO_TESTE);

        String token = jwtUtils.generateToken(authentication);

        assertNotNull(token, "O token não deve ser nulo");
        assertTrue(token.length() > 0, "O token não deve estar vazio");
    }

    @Test
    @DisplayName("Deve validar como TRUE um token gerado corretamente")
    void deveValidarTokenLegitimo() {
        when(authentication.getName()).thenReturn(USUARIO_TESTE);
        String token = jwtUtils.generateToken(authentication);

        boolean eValido = jwtUtils.validateToken(token);

        assertTrue(eValido, "O token legítimo deve ser validado com sucesso");
    }

    @Test
    @DisplayName("Deve retornar FALSE para um token malformado ou inválido")
    void deveRetornarFalsoParaTokenInvalido() {
        String tokenInvalido = "token.totalmente.invalido";

        boolean eValido = jwtUtils.validateToken(tokenInvalido);

        assertFalse(eValido, "Um token inválido deve resultar em falso");
    }

    @Test
    @DisplayName("Deve extrair o nome de usuário (Subject) de dentro de um token válido")
    void deveExtrairUsernameDoToken() {
        when(authentication.getName()).thenReturn(USUARIO_TESTE);
        String token = jwtUtils.generateToken(authentication);

        String usernameExtraido = jwtUtils.getUsernameFromToken(token);

        assertEquals(USUARIO_TESTE, usernameExtraido, "O username extraído deve ser igual ao original");
    }
}