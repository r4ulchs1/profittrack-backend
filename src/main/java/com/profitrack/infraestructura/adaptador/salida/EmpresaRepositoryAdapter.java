package com.profitrack.infraestructura.adaptador.salida;

import com.profitrack.dominio.model.Empresa;
import com.profitrack.dominio.puerto.salida.EmpresaRepository;
import com.profitrack.infraestructura.repository.EmpresaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.apache.bcel.classfile.Module;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmpresaRepositoryAdapter implements EmpresaRepository {

    private final EmpresaJpaRepository jpaRepository;

    @Override
    public Empresa guardar(Empresa empresa) {
        return jpaRepository.save(empresa);
    }

    @Override
    public Optional<Empresa> buscarPorId(Long id){
        return jpaRepository.findById(id);
    }

    @Override
    public List<Empresa> buscarActivos() {
        return jpaRepository.findAllByActivoTrue();
    }

}
