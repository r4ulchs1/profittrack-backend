package com.profitrack.infraestructura.repository;

import com.profitrack.dominio.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpresaJpaRepository extends JpaRepository<Empresa, Long> {
    List<Empresa> findAllByActivoTrue();
}
