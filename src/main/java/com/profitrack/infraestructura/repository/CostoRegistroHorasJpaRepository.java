package com.profitrack.infraestructura.repository;

import com.profitrack.dominio.model.CostoRegistroHoras;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CostoRegistroHorasJpaRepository extends JpaRepository<CostoRegistroHoras, Long> {
    @EntityGraph(attributePaths = {"registroHoras", "registroHoras.empleado", "registroHoras.proyecto"})
    List<CostoRegistroHoras> findAllByRegistroHorasProyectoId(Long proyectoId);

    @EntityGraph(attributePaths = {"registroHoras", "registroHoras.empleado", "registroHoras.proyecto"})
    List<CostoRegistroHoras> findAllByRegistroHorasEmpleadoId(Long empleadoId);

    void deleteByRegistroHorasId(Long registroHorasId);
}
