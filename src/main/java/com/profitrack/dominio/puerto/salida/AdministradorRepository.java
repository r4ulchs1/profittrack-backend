package com.profitrack.dominio.puerto.salida;

import com.profitrack.dominio.model.Administrador;
import java.util.Optional;

public interface AdministradorRepository {
    Optional<Administrador> buscarPorCorreoYActivo(String correo);
    Optional<Administrador> buscarPorId(Long id);
    Administrador guardar(Administrador administrador);
}
