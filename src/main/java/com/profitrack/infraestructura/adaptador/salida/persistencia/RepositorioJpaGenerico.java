package com.profitrack.infraestructura.adaptador.salida.persistencia;

import com.profitrack.dominio.model.BaseEntity;
import com.profitrack.dominio.puerto.salida.RepositorioGenerico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface RepositorioJpaGenerico<T extends BaseEntity>
        extends JpaRepository<T, Long>, RepositorioGenerico<T> {

    @Override
    default Optional<T> buscarPorId(Long id) {
        return findById(id);
    }

    @Override
    default List<T> buscarTodos() {
        return findAll();
    }

    @Override
    default T guardar(T entidad) {
        return save(entidad);
    }

    @Override
    default boolean existePorId(Long id) {
        return existsById(id);
    }

    @Override
    default void eliminarPorId(Long id) {
        deleteById(id);
    }
}
