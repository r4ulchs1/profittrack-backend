package com.profitrack.infraestructura.adaptador.salida;

import com.profitrack.dominio.model.Duenio;
import com.profitrack.dominio.puerto.salida.DuenioRepository;
import com.profitrack.infraestructura.repository.DuenioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DuenioRepositoryAdapter implements DuenioRepository {

    private final DuenioJpaRepository jpaRepository;

    @Override
    public Duenio guardar(Duenio duenio) {
        return jpaRepository.save(duenio);
    }

    @Override
    public Optional<Duenio> buscarPorId(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Duenio> buscarActivosPorEmpresa(Long empresaId) {
        return jpaRepository.findAllByEmpresaIdAndActivoTrue(empresaId);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return jpaRepository.existsByCorreo(correo);
    }
}