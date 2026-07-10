package com.profitrack.dominio.puerto.salida;

import com.profitrack.dominio.model.Proyecto;

import java.util.List;
import java.util.Optional;

public interface ProyectoRepository {
    Proyecto guardar(Proyecto proyecto);
    Optional<Proyecto> buscarPorId(Long id);
    List<Proyecto> buscarActivosPorEmpresa(Long empresaId);
    List<Proyecto> buscarInactivosPorEmpresa(Long empresaId);
    List<Proyecto> buscarTodos();
}
