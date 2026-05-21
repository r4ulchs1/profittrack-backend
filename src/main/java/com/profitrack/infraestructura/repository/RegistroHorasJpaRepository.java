package com.profitrack.infraestructura.repository;
import com.profitrack.dominio.model.RegistroHoras;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RegistroHorasJpaRepository extends JpaRepository<RegistroHoras, Long> {
    List<RegistroHoras> findAllByProyectoIdAndActivoTrue(Long proyectoId);
    List<RegistroHoras> findAllByEmpleadoIdAndActivoTrue(Long empleadoId);
    List<RegistroHoras> findAllByProyectoEmpresaIdAndActivoTrue(Long empresaId);
    List<RegistroHoras> findAllByTareaIdAndActivoTrue(Long tareaId);
}
