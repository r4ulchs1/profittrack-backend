package com.profitrack.dominio.puerto.salida;
import com.profitrack.dominio.model.Ingreso;
import java.util.List;
import java.util.Optional;
public interface IngresoRepository {
    Ingreso guardar(Ingreso i);
    Optional<Ingreso> buscarPorId(Long id);
    List<Ingreso> buscarActivosPorEmpresa(Long empresaId);
    List<Ingreso> buscarActivosPorProyecto(Long proyectoId);
}
