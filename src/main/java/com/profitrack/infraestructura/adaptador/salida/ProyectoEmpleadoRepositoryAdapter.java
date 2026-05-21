package com.profitrack.infraestructura.adaptador.salida;
import com.profitrack.dominio.model.ProyectoEmpleado;
import com.profitrack.dominio.puerto.salida.ProyectoEmpleadoRepository;
import com.profitrack.infraestructura.repository.ProyectoEmpleadoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
@Component @RequiredArgsConstructor
public class ProyectoEmpleadoRepositoryAdapter implements ProyectoEmpleadoRepository {
    private final ProyectoEmpleadoJpaRepository jpa;
    @Override public ProyectoEmpleado guardar(ProyectoEmpleado pe) { return jpa.save(pe); }
    @Override public Optional<ProyectoEmpleado> buscarPorId(Long id) { return jpa.findById(id); }
    @Override public List<ProyectoEmpleado> buscarActivosPorProyecto(Long proyectoId) { return jpa.findAllByProyectoIdAndActivoTrue(proyectoId); }
    @Override public List<ProyectoEmpleado> buscarActivosPorEmpleado(Long empleadoId) { return jpa.findAllByEmpleadoIdAndActivoTrue(empleadoId); }
}
