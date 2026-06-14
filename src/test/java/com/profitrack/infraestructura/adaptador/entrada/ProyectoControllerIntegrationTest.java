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

import jakarta.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ProyectoController – Pruebas de Integración")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProyectoControllerIntegrationTest {

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
        private TipoServicioJpaRepository tipoServicioRepo;
        @Autowired
        private ProyectoJpaRepository proyectoRepo;
        @Autowired
        private PasswordEncoder passwordEncoder;

        private Empresa empresa;
        private TipoServicio tipoServicio;

        @BeforeEach
        void setUp() {
                empresa = empresaRepo.save(Empresa.builder()
                                .nombre("Test Corp")
                                .ruc("20100000002")
                                .build());

                tipoServicio = tipoServicioRepo.save(TipoServicio.builder()
                                .empresa(empresa)
                                .nombre("Desarrollo Web")
                                .descripcion("Desarrollo de aplicaciones web")
                                .build());
        }

        // Helper: hace login y extrae la cookie access_token
        private Cookie loginYObtenerCookie(String correo, String contrasenia) throws Exception {
                String loginBody = String.format(
                                "{\"correo\": \"%s\", \"contrasenia\": \"%s\"}", correo, contrasenia);

                MvcResult result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody))
                                .andExpect(status().isOk())
                                .andReturn();

                Cookie accessCookie = result.getResponse().getCookie("access_token");
                if (accessCookie == null) {
                        throw new RuntimeException("No se obtuvo cookie access_token después del login");
                }
                return accessCookie;
        }

        // ────────────────────────────────────────────
        // ACCESO NO AUTENTICADO
        // ────────────────────────────────────────────
        @Test
        @Order(1)
        @DisplayName("Acceso sin autenticación retorna 401")
        void listarProyectos_sinAuth_retorna401() throws Exception {
                mockMvc.perform(get("/api/proyectos"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @Order(2)
        @DisplayName("Crear proyecto sin autenticación retorna 401")
        void crearProyecto_sinAuth_retorna401() throws Exception {
                String body = """
                                {"nombre": "Proyecto", "tipoServicioId": 1}
                                """;

                mockMvc.perform(post("/api/proyectos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isUnauthorized());
        }

        // ────────────────────────────────────────────
        // OPERACIONES AUTENTICADAS COMO DUEÑO
        // ────────────────────────────────────────────
        @Test
        @Order(3)
        @DisplayName("Dueño puede listar proyectos de su empresa")
        void listarProyectos_comoDuenio_retorna200() throws Exception {
                // Arrange
                duenioRepo.save(Duenio.builder()
                                .empresa(empresa)
                                .nombres("Carlos").apellidos("Rivera")
                                .correo("owner2@test.com")
                                .contrasenia(passwordEncoder.encode("owner123"))
                                .build());

                proyectoRepo.save(Proyecto.builder()
                                .empresa(empresa)
                                .tipoServicio(tipoServicio)
                                .nombre("Proyecto Alpha")
                                .codigo("PRY-ALPHA")
                                .estado(EstadoProyecto.EN_PROCESO)
                                .build());

                Cookie accessCookie = loginYObtenerCookie("owner2@test.com", "owner123");

                mockMvc.perform(get("/api/proyectos")
                                .cookie(accessCookie))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].nombre").value("Proyecto Alpha"));
        }

        @Test
        @Order(4)
        @DisplayName("Dueño puede crear un proyecto con etapas")
        void crearProyecto_comoDuenio_retorna201() throws Exception {
                duenioRepo.save(Duenio.builder()
                                .empresa(empresa)
                                .nombres("Carlos").apellidos("Rivera")
                                .correo("owner3@test.com")
                                .contrasenia(passwordEncoder.encode("owner123"))
                                .build());

                Cookie accessCookie = loginYObtenerCookie("owner3@test.com", "owner123");

                String body = String.format("""
                                {
                                    "tipoServicioId": %d,
                                    "nombre": "Proyecto Nuevo",
                                    "codigo": "PRY-NEW",
                                    "etapas": [
                                        {"nombre": "Análisis", "horasPlanificadas": 40},
                                        {"nombre": "Desarrollo", "horasPlanificadas": 60}
                                    ]
                                }
                                """, tipoServicio.getId());

                mockMvc.perform(post("/api/proyectos")
                                .cookie(accessCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.nombre").value("Proyecto Nuevo"))
                                .andExpect(jsonPath("$.estado").value("PLANIFICADO"))
                                .andExpect(jsonPath("$.etapas").isArray())
                                .andExpect(jsonPath("$.etapas.length()").value(2));
        }

        // ────────────────────────────────────────────
        // RESTRICCIÓN POR ROL
        // ────────────────────────────────────────────
        @Test
        @Order(5)
        @DisplayName("Desarrollador NO puede crear proyectos (solo tiene acceso limitado)")
        void crearProyecto_comoDev_retorna500OError() throws Exception {
                Rol rolDev = rolRepo.save(Rol.builder()
                                .empresa(empresa).nombre("Desarrollador").descripcion("Dev").build());

                empleadoRepo.save(Empleado.builder()
                                .empresa(empresa).rol(rolDev)
                                .nombres("Luis").apellidos("Fernandez")
                                .correo("dev2@test.com")
                                .contrasenia(passwordEncoder.encode("dev123"))
                                .build());

                Cookie accessCookie = loginYObtenerCookie("dev2@test.com", "dev123");

                String body = String.format("""
                                {"tipoServicioId": %d, "nombre": "Proyecto Hack"}
                                """, tipoServicio.getId());

                // Esperamos error de permisos (RuntimeException lanzada por validarRol)
                mockMvc.perform(post("/api/proyectos")
                                .cookie(accessCookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isForbidden());
        }

        // ────────────────────────────────────────────
        // ELIMINAR PROYECTO
        // ────────────────────────────────────────────
        @Test
        @Order(6)
        @DisplayName("Dueño puede eliminar (soft delete) un proyecto")
        void eliminarProyecto_comoDuenio_retorna204() throws Exception {
                duenioRepo.save(Duenio.builder()
                                .empresa(empresa)
                                .nombres("Carlos").apellidos("Rivera")
                                .correo("owner4@test.com")
                                .contrasenia(passwordEncoder.encode("owner123"))
                                .build());

                Proyecto proyecto = proyectoRepo.save(Proyecto.builder()
                                .empresa(empresa)
                                .tipoServicio(tipoServicio)
                                .nombre("A eliminar")
                                .codigo("PRY-DEL")
                                .estado(EstadoProyecto.PLANIFICADO)
                                .build());

                Cookie accessCookie = loginYObtenerCookie("owner4@test.com", "owner123");

                mockMvc.perform(delete("/api/proyectos/" + proyecto.getId())
                                .cookie(accessCookie))
                                .andExpect(status().isNoContent());

                // Verificar que sigue en BD pero inactivo
                Proyecto eliminado = proyectoRepo.findById(proyecto.getId()).orElseThrow();
                org.assertj.core.api.Assertions.assertThat(eliminado.getActivo()).isFalse();
        }
}
