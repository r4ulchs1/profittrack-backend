package com.profitrack.infraestructura.repository;

import com.profitrack.dominio.model.TareaProyecto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TareaProyectoJpaRepository extends JpaRepository<TareaProyecto, Long> {
    @Override
    @EntityGraph(attributePaths = { "proyecto", "proyecto.empresa", "etapaProyecto", "etapaProyecto.proyecto",
            "tipoTarea", "empleadoAsignado" })
    Optional<TareaProyecto> findById(Long id);

    @EntityGraph(attributePaths = { "proyecto", "proyecto.empresa", "etapaProyecto", "etapaProyecto.proyecto",
            "tipoTarea", "empleadoAsignado" })
    List<TareaProyecto> findAllByProyectoIdAndActivoTrue(Long proyectoId);

    @EntityGraph(attributePaths = { "proyecto", "proyecto.empresa", "etapaProyecto", "etapaProyecto.proyecto",
            "tipoTarea", "empleadoAsignado" })
    List<TareaProyecto> findAllByProyectoIdAndActivoFalse(Long proyectoId);

    @EntityGraph(attributePaths = { "proyecto", "proyecto.empresa", "etapaProyecto", "etapaProyecto.proyecto",
            "tipoTarea", "empleadoAsignado" })
    List<TareaProyecto> findAllByEtapaProyectoIdAndActivoTrue(Long etapaProyectoId);

    @EntityGraph(attributePaths = { "proyecto", "proyecto.empresa", "etapaProyecto", "etapaProyecto.proyecto",
            "tipoTarea", "empleadoAsignado" })
    List<TareaProyecto> findAllByEtapaProyectoIdInAndActivoTrue(List<Long> etapaProyectoIds);
}
