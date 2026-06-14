package com.profitrack.infraestructura.seguridad;

import com.profitrack.dominio.model.Empleado;
import com.profitrack.dominio.model.Empresa;
import com.profitrack.dominio.model.Proyecto;
import com.profitrack.dominio.model.ProyectoEmpleado;
import com.profitrack.dominio.puerto.salida.ProyectoEmpleadoRepository;
import com.profitrack.dominio.puerto.salida.ProyectoRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityContextUtils – Pruebas de Seguridad y Roles")
class SecurityContextUtilsTest {

    @Mock
    private ProyectoRepository proyectoRepository;
    @Mock
    private ProyectoEmpleadoRepository proyectoEmpleadoRepository;

    @InjectMocks
    private SecurityContextUtils securityContextUtils;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private void simularJwt(String correo, String rolNombre, String tipo, Long empresaId, Long userId) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject(correo)
                .claim("rolNombre", rolNombre)
                .claim("tipo", tipo)
                .claim("empresaId", empresaId)
                .claim("userId", userId)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(jwt);

        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(ctx);
    }

    // ────────────────────────────────────────────
    // EXTRACCIÓN DE CLAIMS
    // ────────────────────────────────────────────
    @Nested
    @DisplayName("Extracción de claims del JWT")
    class ExtraccionClaims {

        @Test
        @DisplayName("Debe extraer el correo del JWT")
        void getCorreo_retornaSubject() {
            simularJwt("pm@techconsult.pe", "PM", "empleado", 1L, 10L);
            assertThat(securityContextUtils.getCorreo()).isEqualTo("pm@techconsult.pe");
        }

        @Test
        @DisplayName("Debe extraer el rol del JWT")
        void getRolNombre_retornaRol() {
            simularJwt("pm@techconsult.pe", "PM", "empleado", 1L, 10L);
            assertThat(securityContextUtils.getRolNombre()).isEqualTo("PM");
        }

        @Test
        @DisplayName("Debe extraer el empresaId del JWT")
        void getEmpresaId_retornaId() {
            simularJwt("pm@techconsult.pe", "PM", "empleado", 1L, 10L);
            assertThat(securityContextUtils.getEmpresaId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Debe extraer el userId del JWT")
        void getUserId_retornaId() {
            simularJwt("pm@techconsult.pe", "PM", "empleado", 1L, 10L);
            assertThat(securityContextUtils.getUserId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Debe extraer el tipo de usuario del JWT")
        void getTipo_retornaTipo() {
            simularJwt("owner@techconsult.pe", "Owner", "duenio", 1L, 1L);
            assertThat(securityContextUtils.getTipo()).isEqualTo("duenio");
        }

        @Test
        @DisplayName("Debe lanzar error si no hay autenticación")
        void getCorreo_sinAuth_lanzaError() {
            SecurityContextHolder.clearContext();
            assertThatThrownBy(() -> securityContextUtils.getCorreo())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No hay usuario autenticado");
        }
    }

    // ────────────────────────────────────────────
    // VALIDACIÓN DE ROLES
    // ────────────────────────────────────────────
    @Nested
    @DisplayName("Validación de roles")
    class ValidacionRoles {

        @Test
        @DisplayName("Debe pasar validación cuando el usuario tiene el rol requerido")
        void validarRol_rolPermitido_noPasa() {
            simularJwt("pm@techconsult.pe", "PM", "empleado", 1L, 10L);
            assertThatCode(() -> securityContextUtils.validarRol("PM", "Gerente"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Debe lanzar error cuando el usuario NO tiene el rol requerido")
        void validarRol_rolNoPermitido_lanzaError() {
            simularJwt("dev@techconsult.pe", "Desarrollador", "empleado", 1L, 20L);
            assertThatThrownBy(() -> securityContextUtils.validarRol("PM", "Gerente"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No tiene permisos");
        }

        @Test
        @DisplayName("El dueño (owner) siempre pasa la validación de roles")
        void validarRol_duenio_siemprePasa() {
            simularJwt("owner@techconsult.pe", "Owner", "duenio", 1L, 1L);
            assertThatCode(() -> securityContextUtils.validarRol("PM"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("tieneRol retorna true para dueño con cualquier rol")
        void tieneRol_duenio_retornaTrue() {
            simularJwt("owner@techconsult.pe", "Owner", "duenio", 1L, 1L);
            assertThat(securityContextUtils.tieneRol("Desarrollador")).isTrue();
        }

        @Test
        @DisplayName("tieneRol retorna false si el empleado no tiene el rol")
        void tieneRol_empleadoSinRol_retornaFalse() {
            simularJwt("dev@techconsult.pe", "Desarrollador", "empleado", 1L, 20L);
            assertThat(securityContextUtils.tieneRol("PM", "Gerente")).isFalse();
        }
    }

    // ────────────────────────────────────────────
    // ACCESO A PROYECTOS
    // ────────────────────────────────────────────
    @Nested
    @DisplayName("Validación de acceso a proyectos")
    class AccesoProyectos {

        private Proyecto crearProyectoDemo(Long proyectoId, Long liderEmpleadoId) {
            Empresa emp = new Empresa();
            emp.setId(1L);

            Empleado liderEmp = null;
            if (liderEmpleadoId != null) {
                liderEmp = new Empleado();
                liderEmp.setId(liderEmpleadoId);
            }

            Proyecto proyecto = Proyecto.builder()
                    .empresa(emp)
                    .liderEmpleado(liderEmp)
                    .nombre("Proyecto Demo")
                    .build();
            proyecto.setId(proyectoId);
            proyecto.setActivo(true);
            return proyecto;
        }

        @Test
        @DisplayName("PM puede acceder a cualquier proyecto de su empresa")
        void validarAcceso_pm_puedeAcceder() {
            simularJwt("pm@techconsult.pe", "PM", "empleado", 1L, 10L);

            Proyecto proyecto = crearProyectoDemo(50L, 10L);
            when(proyectoRepository.buscarPorId(50L)).thenReturn(Optional.of(proyecto));

            assertThatCode(() -> securityContextUtils.validarAccesoProyecto(50L))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Desarrollador asignado puede acceder al proyecto")
        void validarAcceso_devAsignado_puedeAcceder() {
            simularJwt("dev@techconsult.pe", "Desarrollador", "empleado", 1L, 20L);

            Proyecto proyecto = crearProyectoDemo(50L, 10L);
            when(proyectoRepository.buscarPorId(50L)).thenReturn(Optional.of(proyecto));
            when(proyectoEmpleadoRepository.buscarActivoPorProyectoYEmpleado(50L, 20L))
                    .thenReturn(Optional.of(new ProyectoEmpleado()));

            assertThatCode(() -> securityContextUtils.validarAccesoProyecto(50L))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Desarrollador NO asignado NO puede acceder al proyecto")
        void validarAcceso_devNoAsignado_lanzaError() {
            simularJwt("dev@techconsult.pe", "Desarrollador", "empleado", 1L, 20L);

            Proyecto proyecto = crearProyectoDemo(50L, 10L);
            when(proyectoRepository.buscarPorId(50L)).thenReturn(Optional.of(proyecto));
            when(proyectoEmpleadoRepository.buscarActivoPorProyectoYEmpleado(50L, 20L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> securityContextUtils.validarAccesoProyecto(50L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Acceso denegado");
        }

        @Test
        @DisplayName("No puede acceder a proyecto de otra empresa")
        void validarAcceso_otraEmpresa_lanzaError() {
            simularJwt("pm@techconsult.pe", "PM", "empleado", 1L, 10L);

            Empresa otraEmpresa = new Empresa();
            otraEmpresa.setId(99L);
            Proyecto proyecto = Proyecto.builder()
                    .empresa(otraEmpresa).nombre("Ajeno").build();
            proyecto.setId(50L);
            proyecto.setActivo(true);

            when(proyectoRepository.buscarPorId(50L)).thenReturn(Optional.of(proyecto));

            assertThatThrownBy(() -> securityContextUtils.validarAccesoProyecto(50L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("no pertenece a su empresa");
        }
    }
}
