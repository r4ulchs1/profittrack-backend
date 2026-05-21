package com.profitrack.infraestructura.adaptador.salida;
import com.profitrack.dominio.model.RegistroHoras;
import com.profitrack.dominio.puerto.salida.RegistroHorasRepository;
import com.profitrack.infraestructura.repository.RegistroHorasJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
@Component @RequiredArgsConstructor
public class RegistroHorasRepositoryAdapter implements RegistroHorasRepository {
    private final RegistroHorasJpaRepository jpa;
    @Override public RegistroHoras guardar(RegistroHoras rh) { return jpa.save(rh); }
    @Override public Optional<RegistroHoras> buscarPorId(Long id) { return jpa.findById(id); }
    @Override public List<RegistroHoras> buscarActivosPorProyecto(Long proyectoId) { return jpa.findAllByProyectoIdAndActivoTrue(proyectoId); }
    @Override public List<RegistroHoras> buscarActivosPorEmpleado(Long empleadoId) { return jpa.findAllByEmpleadoIdAndActivoTrue(empleadoId); }
    @Override public List<RegistroHoras> buscarActivosPorEmpresa(Long empresaId) { return jpa.findAllByProyectoEmpresaIdAndActivoTrue(empresaId); }
    @Override public List<RegistroHoras> buscarActivosPorTarea(Long tareaId) { return jpa.findAllByTareaIdAndActivoTrue(tareaId); }
}
