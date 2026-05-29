package com.profitrack.infraestructura.repository;

import com.profitrack.dominio.model.TareaProyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TareaProyectoJpaRepository extends JpaRepository<TareaProyecto, Long> {
    List<TareaProyecto> findAllByProyectoIdAndActivoTrue(Long proyectoId);

    List<TareaProyecto> findAllByProyectoIdAndActivoFalse(Long proyectoId);

    List<TareaProyecto> findAllByEtapaProyectoIdAndActivoTrue(Long etapaProyectoId);
}
