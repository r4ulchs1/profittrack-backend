package com.profitrack.infraestructura.repository;

import com.profitrack.dominio.model.RegistroHoras;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RegistroHorasJpaRepository extends JpaRepository<RegistroHoras, Long> {
    @Override
    @EntityGraph(attributePaths = {"empleado", "proyecto", "tarea", "tarea.etapaProyecto", "tarea.proyecto"})
    Optional<RegistroHoras> findById(Long id);

    @EntityGraph(attributePaths = {"empleado", "proyecto", "tarea", "tarea.etapaProyecto", "tarea.proyecto"})
    List<RegistroHoras> findAllByProyectoIdAndActivoTrue(Long proyectoId);

    @EntityGraph(attributePaths = {"empleado", "proyecto", "tarea", "tarea.etapaProyecto", "tarea.proyecto"})
    List<RegistroHoras> findAllByEmpleadoIdAndActivoTrue(Long empleadoId);

    @EntityGraph(attributePaths = {"empleado", "proyecto", "tarea", "tarea.etapaProyecto", "tarea.proyecto"})
    List<RegistroHoras> findAllByProyectoEmpresaIdAndActivoTrue(Long empresaId);

    @EntityGraph(attributePaths = {"empleado", "proyecto", "tarea", "tarea.etapaProyecto", "tarea.proyecto"})
    List<RegistroHoras> findAllByTareaIdAndActivoTrue(Long tareaId);
}
