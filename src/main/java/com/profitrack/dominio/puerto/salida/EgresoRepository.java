package com.profitrack.dominio.puerto.salida;
import com.profitrack.dominio.model.Egreso;
import java.util.List;
import java.util.Optional;
public interface EgresoRepository {
    Egreso guardar(Egreso e);
    Optional<Egreso> buscarPorId(Long id);
    List<Egreso> buscarActivosPorEmpresa(Long empresaId);
    List<Egreso> buscarActivosPorProyecto(Long proyectoId);
}
