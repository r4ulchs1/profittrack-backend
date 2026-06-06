package com.profitrack.infraestructura.repository;

import com.profitrack.dominio.model.EtapaProyecto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtapaProyectoJpaRepository extends JpaRepository<EtapaProyecto, Long> {
    @Override
    @EntityGraph(attributePaths = {"proyecto", "proyecto.empresa"})
    Optional<EtapaProyecto> findById(Long id);

    @EntityGraph(attributePaths = {"proyecto", "proyecto.empresa"})
    List<EtapaProyecto> findAllByProyectoIdAndActivoTrueOrderByOrdenAsc(Long proyectoId);

    @EntityGraph(attributePaths = {"proyecto", "proyecto.empresa"})
    List<EtapaProyecto> findAllByProyectoIdAndActivoFalseOrderByOrdenAsc(Long proyectoId);
}
