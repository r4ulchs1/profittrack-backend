package com.profitrack.infraestructura.adaptador.salida.persistencia;

import com.profitrack.dominio.model.Administrador;
import com.profitrack.dominio.puerto.salida.AdministradorRepository;
import com.profitrack.infraestructura.adaptador.salida.persistencia.repositorio.AdministradorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdministradorRepositoryAdapter implements AdministradorRepository {

    private final AdministradorJpaRepository jpaRepository;

    @Override
    public Optional<Administrador> buscarPorCorreoYActivo(String correo) {
        return jpaRepository.findByCorreoAndActivoTrue(correo);
    }

    @Override
    public Optional<Administrador> buscarPorId(Long id) {
        return jpaRepository.findById(id).filter(Administrador::getActivo);
    }

    @Override
    public Administrador guardar(Administrador administrador) {
        return jpaRepository.save(administrador);
    }
}
