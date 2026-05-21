package com.profitrack.infraestructura.adaptador.salida;
import com.profitrack.dominio.model.Ingreso;
import com.profitrack.dominio.puerto.salida.IngresoRepository;
import com.profitrack.infraestructura.repository.IngresoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
@Component @RequiredArgsConstructor
public class IngresoRepositoryAdapter implements IngresoRepository {
    private final IngresoJpaRepository jpa;
    @Override public Ingreso guardar(Ingreso i) { return jpa.save(i); }
    @Override public Optional<Ingreso> buscarPorId(Long id) { return jpa.findById(id); }
    @Override public List<Ingreso> buscarActivosPorEmpresa(Long empresaId) { return jpa.findAllByEmpresaIdAndActivoTrue(empresaId); }
    @Override public List<Ingreso> buscarActivosPorProyecto(Long proyectoId) { return jpa.findAllByProyectoIdAndActivoTrue(proyectoId); }
}
