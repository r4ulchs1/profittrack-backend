package com.profitrack.infraestructura.adaptador.salida;
import com.profitrack.dominio.model.CostoRegistroHoras;
import com.profitrack.dominio.puerto.salida.CostoRegistroHorasRepository;
import com.profitrack.infraestructura.repository.CostoRegistroHorasJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
@Component @RequiredArgsConstructor
public class CostoRegistroHorasRepositoryAdapter implements CostoRegistroHorasRepository {
    private final CostoRegistroHorasJpaRepository jpa;
    @Override public CostoRegistroHoras guardar(CostoRegistroHoras c) { return jpa.save(c); }
    @Override public List<CostoRegistroHoras> buscarPorProyecto(Long proyectoId) { return jpa.findAllByRegistroHorasProyectoId(proyectoId); }
    @Override public List<CostoRegistroHoras> buscarPorEmpleado(Long empleadoId) { return jpa.findAllByRegistroHorasEmpleadoId(empleadoId); }
}
