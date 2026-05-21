package com.profitrack.infraestructura.repository;
import com.profitrack.dominio.model.Egreso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface EgresoJpaRepository extends JpaRepository<Egreso, Long> {
    List<Egreso> findAllByEmpresaIdAndActivoTrue(Long empresaId);
    List<Egreso> findAllByProyectoIdAndActivoTrue(Long proyectoId);
}
