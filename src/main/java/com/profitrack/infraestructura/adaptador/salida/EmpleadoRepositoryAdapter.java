package com.profitrack.infraestructura.adaptador.salida;

import com.profitrack.dominio.model.Empleado;
import com.profitrack.dominio.puerto.salida.EmpleadoRepository;
import com.profitrack.infraestructura.repository.EmpleadoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmpleadoRepositoryAdapter implements EmpleadoRepository {

    private final EmpleadoJpaRepository jpaRepository;

    @Override
    public Empleado guardar(Empleado empleado) {
        return jpaRepository.save(empleado);
    }

    @Override
    public Optional<Empleado> buscarPorId(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Empleado> buscarActivosPorEmpresa(Long empresaId) {
        return jpaRepository.findAllByEmpresaIdAndActivoTrue(empresaId);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return jpaRepository.existsByCorreo(correo);
    }

    @Override
    public Optional<Empleado> buscarPorCorreoYActivo(String correo) {
        return jpaRepository.findByCorreoAndActivoTrue(correo);
    }
}
