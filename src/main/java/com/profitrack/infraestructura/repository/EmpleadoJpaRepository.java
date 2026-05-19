package com.profitrack.infraestructura.repository;

import com.profitrack.dominio.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpleadoJpaRepository extends JpaRepository<Empleado, Long> {
    List<Empleado> findAllByEmpresaIdAndActivoTrue(Long empresaId);
    boolean existsByCorreo(String correo);
    boolean existsByCorreoAndIdNot(String correo, Long id);
    Optional<Empleado> findByCorreoAndActivoTrue(String correo);
}
