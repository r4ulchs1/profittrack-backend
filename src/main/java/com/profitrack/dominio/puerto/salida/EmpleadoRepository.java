package com.profitrack.dominio.puerto.salida;

import com.profitrack.dominio.model.Empleado;

import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository {
    Empleado guardar(Empleado empleado);
    Optional<Empleado> buscarPorId(Long id);
    List<Empleado> buscarActivosPorEmpresa(Long empresaId);
    List<Empleado> buscarInactivosPorEmpresa(Long empresaId);
    boolean existePorCorreo(String correo);
    Optional<Empleado> buscarPorCorreoYActivo(String correo);
    List<Empleado> buscarTodos();
}
