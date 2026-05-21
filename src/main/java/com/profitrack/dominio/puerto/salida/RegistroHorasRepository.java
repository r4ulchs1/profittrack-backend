package com.profitrack.dominio.puerto.salida;
import com.profitrack.dominio.model.RegistroHoras;
import java.util.List;
import java.util.Optional;
public interface RegistroHorasRepository {
    RegistroHoras guardar(RegistroHoras rh);
    Optional<RegistroHoras> buscarPorId(Long id);
    List<RegistroHoras> buscarActivosPorProyecto(Long proyectoId);
    List<RegistroHoras> buscarActivosPorEmpleado(Long empleadoId);
    List<RegistroHoras> buscarActivosPorEmpresa(Long empresaId);
    List<RegistroHoras> buscarActivosPorTarea(Long tareaId);
}
