package com.profitrack.dominio.puerto.salida;

import com.profitrack.dominio.model.Empresa;

import java.util.List;
import java.util.Optional;

public interface EmpresaRepository {
    Empresa guardar(Empresa empresa);
    Optional<Empresa> buscarPorId(Long id);
    List<Empresa> buscarActivos();
}