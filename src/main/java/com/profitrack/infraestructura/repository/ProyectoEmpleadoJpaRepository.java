package com.profitrack.infraestructura.repository;
import com.profitrack.dominio.model.ProyectoEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ProyectoEmpleadoJpaRepository extends JpaRepository<ProyectoEmpleado, Long> {
    List<ProyectoEmpleado> findAllByProyectoIdAndActivoTrue(Long proyectoId);
    List<ProyectoEmpleado> findAllByEmpleadoIdAndActivoTrue(Long empleadoId);
}
