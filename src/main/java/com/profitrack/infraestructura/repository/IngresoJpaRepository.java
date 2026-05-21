package com.profitrack.infraestructura.repository;
import com.profitrack.dominio.model.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface IngresoJpaRepository extends JpaRepository<Ingreso, Long> {
    List<Ingreso> findAllByEmpresaIdAndActivoTrue(Long empresaId);
    List<Ingreso> findAllByProyectoIdAndActivoTrue(Long proyectoId);
}
