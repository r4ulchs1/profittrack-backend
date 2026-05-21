package com.profitrack.dominio.service;

import com.profitrack.aplicacion.dto.proyectoDto.ProyectoPatchDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoRequestDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoResponseDto;
import com.profitrack.dominio.model.*;
import com.profitrack.dominio.puerto.entrada.ProyectoUseCase;
import com.profitrack.dominio.puerto.salida.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProyectoService implements ProyectoUseCase {

    private final ProyectoRepository proyectoRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final TipoServicioRepository tipoServicioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ProyectoEmpleadoRepository proyectoEmpleadoRepository;

    @Override
    public ProyectoResponseDto crear(ProyectoRequestDto dto) {
        Empresa empresa = empresaRepository.buscarPorId(dto.getEmpresaId())
                .filter(Empresa::getActivo)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con id: " + dto.getEmpresaId()));

        TipoServicio tipo = tipoServicioRepository.buscarPorId(dto.getTipoServicioId())
                .orElseThrow(() -> new RuntimeException("Tipo de servicio no encontrado con id: " + dto.getTipoServicioId()));

        Cliente cliente = null;
        if (dto.getClienteId() != null) {
            cliente = clienteRepository.buscarPorId(dto.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + dto.getClienteId()));
        }

        Empleado lider = null;
        if (dto.getLiderEmpleadoId() != null) {
            lider = empleadoRepository.buscarPorId(dto.getLiderEmpleadoId())
                    .orElseThrow(() -> new RuntimeException("Líder no encontrado con id: " + dto.getLiderEmpleadoId()));
        }

        Proyecto proyecto = Proyecto.builder()
                .empresa(empresa)
                .cliente(cliente)
                .tipoServicio(tipo)
                .liderEmpleado(lider)
                .codigo(dto.getCodigo())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .fechaInicioPlanificada(dto.getFechaInicioPlanificada())
                .fechaFinPlanificada(dto.getFechaFinPlanificada())
                .horasPlanificadas(dto.getHorasPlanificadas())
                .presupuestoPlanificado(dto.getPresupuestoPlanificado())
                .margenPlanificado(dto.getMargenPlanificado())
                .precioVenta(dto.getPrecioVenta())
                .estado(EstadoProyecto.PLANIFICADO)
                .build();

        return toDto(proyectoRepository.guardar(proyecto));
    }

    @Override
    public ProyectoResponseDto obtenerPorId(Long id) {
        return proyectoRepository.buscarPorId(id)
                .filter(Proyecto::getActivo)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con id: " + id));
    }

    @Override
    public List<ProyectoResponseDto> listarActivosPorEmpresa(Long empresaId) {
        return proyectoRepository.buscarActivosPorEmpresa(empresaId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProyectoResponseDto> listarInactivosPorEmpresa(Long empresaId) {
        return proyectoRepository.buscarInactivosPorEmpresa(empresaId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProyectoResponseDto> listarProyectosAsignados(Long empleadoId, Long empresaId) {
        List<Proyecto> asignados = proyectoEmpleadoRepository.buscarActivosPorEmpleado(empleadoId).stream()
                .map(ProyectoEmpleado::getProyecto)
                .filter(Proyecto::getActivo)
                .filter(p -> p.getEmpresa().getId().equals(empresaId))
                .collect(Collectors.toList());

        List<Proyecto> liderados = proyectoRepository.buscarActivosPorEmpresa(empresaId).stream()
                .filter(p -> p.getLiderEmpleado() != null && p.getLiderEmpleado().getId().equals(empleadoId))
                .collect(Collectors.toList());

        java.util.Set<Long> ids = new java.util.HashSet<>();
        List<Proyecto> todos = new java.util.ArrayList<>();
        for (Proyecto p : asignados) {
            if (ids.add(p.getId())) {
                todos.add(p);
            }
        }
        for (Proyecto p : liderados) {
            if (ids.add(p.getId())) {
                todos.add(p);
            }
        }

        return todos.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ProyectoResponseDto reactivar(Long id) {
        Proyecto proyecto = proyectoRepository.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con id: " + id));
        proyecto.setActivo(true);
        return toDto(proyectoRepository.guardar(proyecto));
    }

    @Override
    public ProyectoResponseDto actualizar(Long id, ProyectoPatchDto dto) {
        Proyecto proyecto = proyectoRepository.buscarPorId(id)
                .filter(Proyecto::getActivo)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con id: " + id));

        if (dto.getClienteId() != null) {
            Cliente c = clienteRepository.buscarPorId(dto.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            proyecto.setCliente(c);
        }
        if (dto.getTipoServicioId() != null) {
            TipoServicio t = tipoServicioRepository.buscarPorId(dto.getTipoServicioId())
                    .orElseThrow(() -> new RuntimeException("Tipo servicio no encontrado"));
            proyecto.setTipoServicio(t);
        }
        if (dto.getLiderEmpleadoId() != null) {
            Empleado l = empleadoRepository.buscarPorId(dto.getLiderEmpleadoId())
                    .orElseThrow(() -> new RuntimeException("Líder no encontrado"));
            proyecto.setLiderEmpleado(l);
        }
        if (dto.getCodigo() != null) proyecto.setCodigo(dto.getCodigo());
        if (dto.getNombre() != null) proyecto.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) proyecto.setDescripcion(dto.getDescripcion());
        if (dto.getFechaInicioPlanificada() != null) proyecto.setFechaInicioPlanificada(dto.getFechaInicioPlanificada());
        if (dto.getFechaFinPlanificada() != null) proyecto.setFechaFinPlanificada(dto.getFechaFinPlanificada());
        if (dto.getFechaInicioReal() != null) proyecto.setFechaInicioReal(dto.getFechaInicioReal());
        if (dto.getFechaFinReal() != null) proyecto.setFechaFinReal(dto.getFechaFinReal());
        if (dto.getHorasPlanificadas() != null) proyecto.setHorasPlanificadas(dto.getHorasPlanificadas());
        if (dto.getPresupuestoPlanificado() != null) proyecto.setPresupuestoPlanificado(dto.getPresupuestoPlanificado());
        if (dto.getMargenPlanificado() != null) proyecto.setMargenPlanificado(dto.getMargenPlanificado());
        if (dto.getPrecioVenta() != null) proyecto.setPrecioVenta(dto.getPrecioVenta());
        if (dto.getEstado() != null) proyecto.setEstado(EstadoProyecto.valueOf(dto.getEstado()));

        return toDto(proyectoRepository.guardar(proyecto));
    }

    @Override
    public void eliminar(Long id) {
        Proyecto proyecto = proyectoRepository.buscarPorId(id)
                .filter(Proyecto::getActivo)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con id: " + id));

        proyecto.setActivo(false);
        proyectoRepository.guardar(proyecto);
    }

    private ProyectoResponseDto toDto(Proyecto p) {
        return ProyectoResponseDto.builder()
                .id(p.getId())
                .empresaId(p.getEmpresa().getId())
                .clienteId(p.getCliente() != null ? p.getCliente().getId() : null)
                .clienteNombre(p.getCliente() != null ? p.getCliente().getRazonSocial() : null)
                .tipoServicioId(p.getTipoServicio().getId())
                .tipoServicioNombre(p.getTipoServicio().getNombre())
                .liderEmpleadoId(p.getLiderEmpleado() != null ? p.getLiderEmpleado().getId() : null)
                .liderNombre(p.getLiderEmpleado() != null ?
                        p.getLiderEmpleado().getNombres() + " " + p.getLiderEmpleado().getApellidos() : null)
                .codigo(p.getCodigo())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .fechaInicioPlanificada(p.getFechaInicioPlanificada())
                .fechaFinPlanificada(p.getFechaFinPlanificada())
                .fechaInicioReal(p.getFechaInicioReal())
                .fechaFinReal(p.getFechaFinReal())
                .horasPlanificadas(p.getHorasPlanificadas())
                .horasReales(p.getHorasReales())
                .presupuestoPlanificado(p.getPresupuestoPlanificado())
                .costoReal(p.getCostoReal())
                .margenPlanificado(p.getMargenPlanificado())
                .margenReal(p.getMargenReal())
                .precioVenta(p.getPrecioVenta())
                .estado(p.getEstado() != null ? p.getEstado().name() : null)
                .activo(p.getActivo())
                .build();
    }
}
