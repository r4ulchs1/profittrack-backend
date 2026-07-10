package com.profitrack.dominio.puerto.salida;

import com.profitrack.dominio.model.Duenio;

import java.util.List;
import java.util.Optional;

public interface DuenioRepository {
    Duenio guardar(Duenio duenio);
    Optional<Duenio> buscarPorId(Long id);
    List<Duenio> buscarActivosPorEmpresa(Long empresaId);
    boolean existePorCorreo(String correo);
    Optional<Duenio> buscarPorCorreoYActivo(String correo);
    List<Duenio> buscarTodos();
}