package com.profitrack.infraestructura.adaptador.salida;

import com.profitrack.dominio.model.TareaProyecto;
import com.profitrack.dominio.puerto.salida.TareaProyectoRepository;
import com.profitrack.infraestructura.repository.TareaProyectoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TareaProyectoRepositoryAdapter implements TareaProyectoRepository {
    private final TareaProyectoJpaRepository jpa;

    @Override
    public TareaProyecto guardar(TareaProyecto t) {
        return jpa.save(t);
    }

    @Override
    public Optional<TareaProyecto> buscarPorId(Long id) {
        return jpa.findById(id);
    }

    @Override
    public List<TareaProyecto> buscarActivasPorProyecto(Long proyectoId) {
        return jpa.findAllByProyectoIdAndActivoTrue(proyectoId);
    }

    @Override
    public List<TareaProyecto> buscarInactivasPorProyecto(Long proyectoId) {
        return jpa.findAllByProyectoIdAndActivoFalse(proyectoId);
    }

    @Override
    public List<TareaProyecto> buscarActivasPorEtapa(Long etapaProyectoId) {
        return jpa.findAllByEtapaProyectoIdAndActivoTrue(etapaProyectoId);
    }

    @Override
    public List<TareaProyecto> buscarActivasPorEtapas(List<Long> etapaProyectoIds) {
        if (etapaProyectoIds == null || etapaProyectoIds.isEmpty()) {
            return List.of();
        }
        return jpa.findAllByEtapaProyectoIdInAndActivoTrue(etapaProyectoIds);
    }
}
