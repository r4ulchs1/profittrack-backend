package com.profitrack.infraestructura.adaptador.entrada;

import com.profitrack.dominio.model.*;
import com.profitrack.infraestructura.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController – Pruebas de Integración")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private EmpleadoJpaRepository empleadoRepo;
        @Autowired
        private DuenioJpaRepository duenioRepo;
        @Autowired
        private EmpresaJpaRepository empresaRepo;
        @Autowired
        private RolJpaRepository rolRepo;
        @Autowired
        private PasswordEncoder passwordEncoder;

        private Empresa empresa;

        @BeforeEach
        void setUp() {
                // Crear empresa de prueba
                empresa = empresaRepo.save(Empresa.builder()
                                .nombre("Test Corp")
                                .ruc("20100000001")
                                .build());
        }

        // ────────────────────────────────────────────
        // LOGIN EXITOSO
        // ────────────────────────────────────────────
        @Test
        @Order(1)
        @DisplayName("Login exitoso como empleado retorna 200 y datos del usuario")
        void login_empleadoValido_retorna200() throws Exception {
                // Crear empleado de prueba
                Rol rolPM = rolRepo.save(Rol.builder()
                                .empresa(empresa)
                                .nombre("PM")
                                .descripcion("Project Manager")
                                .build());

                empleadoRepo.save(Empleado.builder()
                                .empresa(empresa)
                                .rol(rolPM)
                                .nombres("Juan")
                                .apellidos("Perez")
                                .correo("pm@test.com")
                                .contrasenia(passwordEncoder.encode("pm123"))
                                .build());

                String loginBody = """
                                {"correo": "pm@test.com", "contrasenia": "pm123"}
                                """;

                MvcResult result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.mensaje").value("Login exitoso"))
                                .andExpect(jsonPath("$.tipo").value("empleado"))
                                .andExpect(jsonPath("$.nombre").value("Juan Perez"))
                                .andExpect(jsonPath("$.rol").value("PM"))
                                .andExpect(jsonPath("$.empresaId").isNumber())
                                .andReturn();

                // Verificar que se establecieron cookies
                String setCookie = result.getResponse().getHeader("Set-Cookie");
                assertThat(setCookie).isNotNull();
                assertThat(setCookie).contains("access_token");
        }

        @Test
        @Order(2)
        @DisplayName("Login exitoso como dueño retorna 200 con tipo 'duenio'")
        void login_duenioValido_retorna200() throws Exception {

                duenioRepo.save(Duenio.builder()
                                .empresa(empresa)
                                .nombres("Carlos")
                                .apellidos("Rivera")
                                .correo("owner@test.com")
                                .contrasenia(passwordEncoder.encode("owner123"))
                                .build());

                String loginBody = """
                                {"correo": "owner@test.com", "contrasenia": "owner123"}
                                """;

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.tipo").value("duenio"))
                                .andExpect(jsonPath("$.rol").value("owner"))
                                .andExpect(jsonPath("$.nombre").value("Carlos Rivera"));
        }

        // ────────────────────────────────────────────
        // LOGIN FALLIDO
        // ────────────────────────────────────────────
        @Test
        @Order(3)
        @DisplayName("Login con credenciales incorrectas retorna 401")
        void login_contrasenaIncorrecta_retorna401() throws Exception {

                Rol rol = rolRepo.save(Rol.builder()
                                .empresa(empresa).nombre("Dev").descripcion("Developer").build());

                empleadoRepo.save(Empleado.builder()
                                .empresa(empresa).rol(rol)
                                .nombres("Luis").apellidos("Fernandez")
                                .correo("dev@test.com")
                                .contrasenia(passwordEncoder.encode("dev123"))
                                .build());

                String loginBody = """
                                {"correo": "dev@test.com", "contrasenia": "MALA_CLAVE"}
                                """;

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(4)
        @DisplayName("Login con correo inexistente retorna 401")
        void login_correoNoExiste_retorna401() throws Exception {
                String loginBody = """
                                {"correo": "nadie@test.com", "contrasenia": "12345"}
                                """;

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody))
                                .andExpect(status().isUnauthorized());
        }

        // ────────────────────────────────────────────
        // VALIDACIÓN DE ENTRADA
        // ────────────────────────────────────────────
        @Test
        @Order(5)
        @DisplayName("Login sin correo retorna 400 Bad Request")
        void login_sinCorreo_retorna400() throws Exception {
                String loginBody = """
                                {"contrasenia": "12345"}
                                """;

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @Order(6)
        @DisplayName("Login con correo inválido retorna 400 Bad Request")
        void login_correoInvalido_retorna400() throws Exception {
                String loginBody = """
                                {"correo": "esto-no-es-email", "contrasenia": "12345"}
                                """;

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody))
                                .andExpect(status().isBadRequest());
        }

        // ────────────────────────────────────────────
        // LOGOUT
        // ────────────────────────────────────────────
        @Test
        @Order(7)
        @DisplayName("Logout limpia las cookies de sesión")
        void logout_limpiaCookies() throws Exception {
                MvcResult result = mockMvc.perform(post("/api/auth/logout"))
                                .andExpect(status().isNoContent())
                                .andReturn();

                String cookies = result.getResponse().getHeader("Set-Cookie");
                assertThat(cookies).isNotNull();
        }
}
