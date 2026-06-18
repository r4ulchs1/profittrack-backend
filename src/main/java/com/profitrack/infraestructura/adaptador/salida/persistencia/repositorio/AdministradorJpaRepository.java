package com.profitrack.infraestructura.adaptador.salida.persistencia.repositorio;

import com.profitrack.dominio.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorJpaRepository extends JpaRepository<Administrador, Long> {
    Optional<Administrador> findByCorreoAndActivoTrue(String correo);
}
