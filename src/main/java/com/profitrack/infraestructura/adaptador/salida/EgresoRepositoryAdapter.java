package com.profitrack.infraestructura.adaptador.salida;
import com.profitrack.dominio.model.Egreso;
import com.profitrack.dominio.puerto.salida.EgresoRepository;
import com.profitrack.infraestructura.repository.EgresoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
@Component @RequiredArgsConstructor
public class EgresoRepositoryAdapter implements EgresoRepository {
    private final EgresoJpaRepository jpa;
    @Override public Egreso guardar(Egreso e) { return jpa.save(e); }
    @Override public Optional<Egreso> buscarPorId(Long id) { return jpa.findById(id); }
    @Override public List<Egreso> buscarActivosPorEmpresa(Long empresaId) { return jpa.findAllByEmpresaIdAndActivoTrue(empresaId); }
    @Override public List<Egreso> buscarActivosPorProyecto(Long proyectoId) { return jpa.findAllByProyectoIdAndActivoTrue(proyectoId); }
}
