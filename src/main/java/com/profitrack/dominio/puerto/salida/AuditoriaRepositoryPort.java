package com.profitrack.dominio.puerto.salida;

import com.profitrack.dominio.model.Auditoria;

import java.util.List;
import java.util.Optional;

public interface AuditoriaRepositoryPort {
    Auditoria guardar(Auditoria auditoria);
    List<Auditoria> listar();
    Optional<Auditoria> buscarPorId(Long id);

}
