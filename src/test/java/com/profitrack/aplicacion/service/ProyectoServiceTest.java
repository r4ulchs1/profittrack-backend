package com.profitrack.aplicacion.service;

import com.profitrack.aplicacion.dto.etapaProyectoDto.EtapaProyectoRequestDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoRequestDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoResponseDto;
import com.profitrack.dominio.model.*;
import com.profitrack.dominio.puerto.salida.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProyectoService – Pruebas Funcionales")
class ProyectoServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private TipoServicioRepository tipoServicioRepository;
    @Mock
    private EmpleadoRepository empleadoRepository;
    @Mock
    private ProyectoEmpleadoRepository proyectoEmpleadoRepository;
    @Mock
    private EtapaProyectoRepository etapaProyectoRepository;
    @Mock
    private TareaProyectoRepository tareaProyectoRepository;

    @InjectMocks
    private ProyectoService proyectoService;

    private Empresa empresa;
    private TipoServicio tipoServicio;
    private Cliente cliente;
    private Empleado lider;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNombre("TechConsult SAC");
        empresa.setActivo(true);

        tipoServicio = new TipoServicio();
        tipoServicio.setId(1L);
        tipoServicio.setNombre("Desarrollo Web");
        tipoServicio.setEmpresa(empresa);

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setRazonSocial("Banco Digital SAC");

        lider = new Empleado();
        lider.setId(10L);
        lider.setNombres("Juan");
        lider.setApellidos("Perez");
        lider.setEmpresa(empresa);
    }

    // ────────────────────────────────────────────
    // CREAR PROYECTO
    // ────────────────────────────────────────────
    @Nested
    @DisplayName("Crear proyecto")
    class CrearProyecto {

        @Test
        @DisplayName("Debe crear un proyecto exitosamente con datos mínimos")
        void crearProyecto_datosMinimos_retornaDto() {
            ProyectoRequestDto dto = new ProyectoRequestDto();
            dto.setEmpresaId(1L);
            dto.setTipoServicioId(1L);
            dto.setNombre("Nuevo Proyecto");

            Proyecto proyectoGuardado = Proyecto.builder()
                    .empresa(empresa)
                    .tipoServicio(tipoServicio)
                    .nombre("Nuevo Proyecto")
                    .estado(EstadoProyecto.PLANIFICADO)
                    .build();
            proyectoGuardado.setId(100L);
            proyectoGuardado.setActivo(true);

            when(empresaRepository.buscarPorId(1L)).thenReturn(Optional.of(empresa));
            when(tipoServicioRepository.buscarPorId(1L)).thenReturn(Optional.of(tipoServicio));
            when(proyectoRepository.guardar(any(Proyecto.class))).thenReturn(proyectoGuardado);
            when(etapaProyectoRepository.buscarActivasPorProyecto(100L)).thenReturn(List.of());

            ProyectoResponseDto resultado = proyectoService.crear(dto);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(100L);
            assertThat(resultado.getNombre()).isEqualTo("Nuevo Proyecto");
            assertThat(resultado.getEstado()).isEqualTo("PLANIFICADO");
            assertThat(resultado.getActivo()).isTrue();
            verify(proyectoRepository).guardar(any(Proyecto.class));
        }

        @Test
        @DisplayName("Debe crear proyecto con cliente y líder asignados")
        void crearProyecto_conClienteYLider_retornaDto() {
            ProyectoRequestDto dto = new ProyectoRequestDto();
            dto.setEmpresaId(1L);
            dto.setTipoServicioId(1L);
            dto.setClienteId(1L);
            dto.setLiderEmpleadoId(10L);
            dto.setNombre("Proyecto Completo");
            dto.setCodigo("PRY-001");
            dto.setPresupuestoPlanificado(new BigDecimal("50000.00"));
            dto.setPrecioVenta(new BigDecimal("70000.00"));

            Proyecto proyectoGuardado = Proyecto.builder()
                    .empresa(empresa)
                    .tipoServicio(tipoServicio)
                    .cliente(cliente)
                    .liderEmpleado(lider)
                    .nombre("Proyecto Completo")
                    .codigo("PRY-001")
                    .presupuestoPlanificado(new BigDecimal("50000.00"))
                    .precioVenta(new BigDecimal("70000.00"))
                    .estado(EstadoProyecto.PLANIFICADO)
                    .build();
            proyectoGuardado.setId(101L);
            proyectoGuardado.setActivo(true);

            when(empresaRepository.buscarPorId(1L)).thenReturn(Optional.of(empresa));
            when(tipoServicioRepository.buscarPorId(1L)).thenReturn(Optional.of(tipoServicio));
            when(clienteRepository.buscarPorId(1L)).thenReturn(Optional.of(cliente));
            when(empleadoRepository.buscarPorId(10L)).thenReturn(Optional.of(lider));
            when(proyectoRepository.guardar(any(Proyecto.class))).thenReturn(proyectoGuardado);
            when(etapaProyectoRepository.buscarActivasPorProyecto(101L)).thenReturn(List.of());

            ProyectoResponseDto resultado = proyectoService.crear(dto);

            assertThat(resultado.getCodigo()).isEqualTo("PRY-001");
            assertThat(resultado.getClienteNombre()).isEqualTo("Banco Digital SAC");
            assertThat(resultado.getLiderNombre()).isEqualTo("Juan Perez");
            assertThat(resultado.getPresupuestoPlanificado()).isEqualByComparingTo("50000.00");
        }

        @Test
        @DisplayName("Debe crear proyecto con etapas iniciales")
        void crearProyecto_conEtapas_guardaEtapas() {

            EtapaProyectoRequestDto etapa1 = new EtapaProyectoRequestDto();
            etapa1.setNombre("Análisis");
            etapa1.setHorasPlanificadas(new BigDecimal("40.00"));

            EtapaProyectoRequestDto etapa2 = new EtapaProyectoRequestDto();
            etapa2.setNombre("Desarrollo");
            etapa2.setHorasPlanificadas(new BigDecimal("60.00"));

            ProyectoRequestDto dto = new ProyectoRequestDto();
            dto.setEmpresaId(1L);
            dto.setTipoServicioId(1L);
            dto.setNombre("Proyecto con Etapas");
            dto.setEtapas(List.of(etapa1, etapa2));

            Proyecto proyectoGuardado = Proyecto.builder()
                    .empresa(empresa).tipoServicio(tipoServicio)
                    .nombre("Proyecto con Etapas")
                    .horasPlanificadas(new BigDecimal("100.00"))
                    .estado(EstadoProyecto.PLANIFICADO)
                    .build();
            proyectoGuardado.setId(102L);
            proyectoGuardado.setActivo(true);

            when(empresaRepository.buscarPorId(1L)).thenReturn(Optional.of(empresa));
            when(tipoServicioRepository.buscarPorId(1L)).thenReturn(Optional.of(tipoServicio));
            when(proyectoRepository.guardar(any(Proyecto.class))).thenReturn(proyectoGuardado);
            when(etapaProyectoRepository.buscarActivasPorProyecto(102L)).thenReturn(List.of());

            proyectoService.crear(dto);

            verify(etapaProyectoRepository, times(2)).guardar(any(EtapaProyecto.class));
        }

        @Test
        @DisplayName("Debe lanzar error si la empresa no existe")
        void crearProyecto_empresaNoExiste_lanzaError() {

            ProyectoRequestDto dto = new ProyectoRequestDto();
            dto.setEmpresaId(999L);
            dto.setTipoServicioId(1L);
            dto.setNombre("Fallido");

            when(empresaRepository.buscarPorId(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> proyectoService.crear(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Empresa no encontrada");
        }

        @Test
        @DisplayName("Debe lanzar error si el tipo de servicio no existe")
        void crearProyecto_tipoServicioNoExiste_lanzaError() {

            ProyectoRequestDto dto = new ProyectoRequestDto();
            dto.setEmpresaId(1L);
            dto.setTipoServicioId(999L);
            dto.setNombre("Fallido");

            when(empresaRepository.buscarPorId(1L)).thenReturn(Optional.of(empresa));
            when(tipoServicioRepository.buscarPorId(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> proyectoService.crear(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Tipo de servicio no encontrado");
        }

        @Test
        @DisplayName("Debe lanzar error si las horas del proyecto difieren de las etapas")
        void crearProyecto_horasNoCoinciden_lanzaError() {

            EtapaProyectoRequestDto etapa = new EtapaProyectoRequestDto();
            etapa.setNombre("Etapa 1");
            etapa.setHorasPlanificadas(new BigDecimal("50.00"));

            ProyectoRequestDto dto = new ProyectoRequestDto();
            dto.setEmpresaId(1L);
            dto.setTipoServicioId(1L);
            dto.setNombre("Fallido");
            dto.setHorasPlanificadas(new BigDecimal("100.00")); // No coincide con la etapa (50)
            dto.setEtapas(List.of(etapa));

            when(empresaRepository.buscarPorId(1L)).thenReturn(Optional.of(empresa));
            when(tipoServicioRepository.buscarPorId(1L)).thenReturn(Optional.of(tipoServicio));

            assertThatThrownBy(() -> proyectoService.crear(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("horas planificadas");
        }
    }

    // ────────────────────────────────────────────
    // OBTENER PROYECTO
    // ────────────────────────────────────────────
    @Nested
    @DisplayName("Obtener proyecto")
    class ObtenerProyecto {

        @Test
        @DisplayName("Debe obtener un proyecto existente por ID")
        void obtenerPorId_existeYActivo_retornaDto() {

            Proyecto proyecto = Proyecto.builder()
                    .empresa(empresa).tipoServicio(tipoServicio)
                    .nombre("Mi Proyecto").estado(EstadoProyecto.EN_PROCESO)
                    .build();
            proyecto.setId(1L);
            proyecto.setActivo(true);

            when(proyectoRepository.buscarPorId(1L)).thenReturn(Optional.of(proyecto));
            when(etapaProyectoRepository.buscarActivasPorProyecto(1L)).thenReturn(List.of());

            ProyectoResponseDto resultado = proyectoService.obtenerPorId(1L);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getNombre()).isEqualTo("Mi Proyecto");
            assertThat(resultado.getEstado()).isEqualTo("EN_PROCESO");
        }

        @Test
        @DisplayName("Debe lanzar error si el proyecto no existe")
        void obtenerPorId_noExiste_lanzaError() {
            when(proyectoRepository.buscarPorId(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> proyectoService.obtenerPorId(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Proyecto no encontrado");
        }

        @Test
        @DisplayName("Debe lanzar error si el proyecto está inactivo")
        void obtenerPorId_inactivo_lanzaError() {
            Proyecto proyecto = Proyecto.builder()
                    .empresa(empresa).tipoServicio(tipoServicio)
                    .nombre("Eliminado").estado(EstadoProyecto.CANCELADO)
                    .build();
            proyecto.setId(1L);
            proyecto.setActivo(false);

            when(proyectoRepository.buscarPorId(1L)).thenReturn(Optional.of(proyecto));

            assertThatThrownBy(() -> proyectoService.obtenerPorId(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Proyecto no encontrado");
        }
    }

    // ────────────────────────────────────────────
    // LISTAR PROYECTOS
    // ────────────────────────────────────────────
    @Nested
    @DisplayName("Listar proyectos")
    class ListarProyectos {

        @Test
        @DisplayName("Debe listar proyectos activos de una empresa")
        void listarActivos_retornaLista() {
            Proyecto p1 = Proyecto.builder()
                    .empresa(empresa).tipoServicio(tipoServicio)
                    .nombre("Proyecto A").estado(EstadoProyecto.EN_PROCESO).build();
            p1.setId(1L);
            p1.setActivo(true);

            Proyecto p2 = Proyecto.builder()
                    .empresa(empresa).tipoServicio(tipoServicio)
                    .nombre("Proyecto B").estado(EstadoProyecto.PLANIFICADO).build();
            p2.setId(2L);
            p2.setActivo(true);

            when(proyectoRepository.buscarActivosPorEmpresa(1L)).thenReturn(List.of(p1, p2));
            when(etapaProyectoRepository.buscarActivasPorProyecto(anyLong())).thenReturn(List.of());

            List<ProyectoResponseDto> resultado = proyectoService.listarActivosPorEmpresa(1L);

            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getNombre()).isEqualTo("Proyecto A");
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay proyectos")
        void listarActivos_sinProyectos_retornaVacia() {
            when(proyectoRepository.buscarActivosPorEmpresa(1L)).thenReturn(List.of());

            List<ProyectoResponseDto> resultado = proyectoService.listarActivosPorEmpresa(1L);

            assertThat(resultado).isEmpty();
        }
    }

    // ────────────────────────────────────────────
    // ELIMINAR (SOFT DELETE)
    // ────────────────────────────────────────────
    @Nested
    @DisplayName("Eliminar proyecto (soft delete)")
    class EliminarProyecto {

        @Test
        @DisplayName("Debe desactivar un proyecto existente")
        void eliminar_existeYActivo_desactiva() {
            Proyecto proyecto = Proyecto.builder()
                    .empresa(empresa).tipoServicio(tipoServicio)
                    .nombre("A eliminar").estado(EstadoProyecto.EN_PROCESO).build();
            proyecto.setId(1L);
            proyecto.setActivo(true);

            when(proyectoRepository.buscarPorId(1L)).thenReturn(Optional.of(proyecto));
            when(proyectoRepository.guardar(any(Proyecto.class))).thenReturn(proyecto);

            proyectoService.eliminar(1L);

            assertThat(proyecto.getActivo()).isFalse();
            verify(proyectoRepository).guardar(proyecto);
        }

        @Test
        @DisplayName("Debe lanzar error al eliminar proyecto inexistente")
        void eliminar_noExiste_lanzaError() {
            when(proyectoRepository.buscarPorId(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> proyectoService.eliminar(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Proyecto no encontrado");
        }
    }

    // ────────────────────────────────────────────
    // REACTIVAR
    // ────────────────────────────────────────────
    @Nested
    @DisplayName("Reactivar proyecto")
    class ReactivarProyecto {

        @Test
        @DisplayName("Debe reactivar un proyecto desactivado")
        void reactivar_proyectoInactivo_loReactiva() {
            Proyecto proyecto = Proyecto.builder()
                    .empresa(empresa).tipoServicio(tipoServicio)
                    .nombre("Desactivado").estado(EstadoProyecto.CANCELADO).build();
            proyecto.setId(1L);
            proyecto.setActivo(false);

            when(proyectoRepository.buscarPorId(1L)).thenReturn(Optional.of(proyecto));
            when(proyectoRepository.guardar(any(Proyecto.class))).thenAnswer(inv -> inv.getArgument(0));
            when(etapaProyectoRepository.buscarActivasPorProyecto(1L)).thenReturn(List.of());

            ProyectoResponseDto resultado = proyectoService.reactivar(1L);

            assertThat(resultado.getActivo()).isTrue();
        }
    }
}
