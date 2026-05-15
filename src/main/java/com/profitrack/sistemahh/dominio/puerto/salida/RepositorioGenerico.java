package com.profitrack.sistemahh.dominio.puerto.salida;

import com.profitrack.sistemahh.dominio.modelo.BaseEntity;

import java.util.List;
import java.util.Optional;

public interface RepositorioGenerico<T extends BaseEntity> {

    Optional<T> buscarPorId(Long id);

    List<T> buscarTodos();

    T guardar(T entidad);

    boolean existePorId(Long id);

    void eliminarPorId(Long id);
}
