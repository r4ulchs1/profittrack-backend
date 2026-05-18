package com.profitrack.infraestructura.repository;

import com.profitrack.dominio.model.Duenio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DuenioJpaRepository extends JpaRepository<Duenio, Long> {
    List<Duenio> findAllByEmpresaIdAndActivoTrue(Long empresaId);
    boolean existsByCorreo(String correo);
}