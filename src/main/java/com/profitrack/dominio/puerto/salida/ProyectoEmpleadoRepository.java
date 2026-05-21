package com.profitrack.dominio.puerto.salida;
import com.profitrack.dominio.model.ProyectoEmpleado;
import java.util.List;
import java.util.Optional;
public interface ProyectoEmpleadoRepository {
    ProyectoEmpleado guardar(ProyectoEmpleado pe);
    Optional<ProyectoEmpleado> buscarPorId(Long id);
    List<ProyectoEmpleado> buscarActivosPorProyecto(Long proyectoId);
    List<ProyectoEmpleado> buscarActivosPorEmpleado(Long empleadoId);
}
