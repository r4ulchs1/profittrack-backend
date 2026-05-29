package com.profitrack.dominio.puerto.salida;
import com.profitrack.dominio.model.TareaProyecto;
import java.util.List;
import java.util.Optional;
public interface TareaProyectoRepository {
    TareaProyecto guardar(TareaProyecto t);
    Optional<TareaProyecto> buscarPorId(Long id);
    List<TareaProyecto> buscarActivasPorProyecto(Long proyectoId);
    List<TareaProyecto> buscarInactivasPorProyecto(Long proyectoId);
    List<TareaProyecto> buscarActivasPorEtapa(Long etapaProyectoId);
}
