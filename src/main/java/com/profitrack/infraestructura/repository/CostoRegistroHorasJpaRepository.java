package com.profitrack.infraestructura.repository;
import com.profitrack.dominio.model.CostoRegistroHoras;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CostoRegistroHorasJpaRepository extends JpaRepository<CostoRegistroHoras, Long> {
    List<CostoRegistroHoras> findAllByRegistroHorasProyectoId(Long proyectoId);
    List<CostoRegistroHoras> findAllByRegistroHorasEmpleadoId(Long empleadoId);
}
