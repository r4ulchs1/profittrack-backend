package com.profitrack.infraestructura.repository;

import com.profitrack.dominio.model.Duenio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DuenioJpaRepository extends JpaRepository<Duenio, Long> {
    List<Duenio> findAllByEmpresaIdAndActivoTrue(Long empresaId);
    boolean existsByCorreo(String correo);
    Optional<Duenio> findByCorreoAndActivoTrue(String correo);
}