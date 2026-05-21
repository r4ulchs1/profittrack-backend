package com.profitrack.dominio.puerto.salida;
import com.profitrack.dominio.model.CostoRegistroHoras;
import java.util.List;
public interface CostoRegistroHorasRepository {
    CostoRegistroHoras guardar(CostoRegistroHoras c);
    List<CostoRegistroHoras> buscarPorProyecto(Long proyectoId);
    List<CostoRegistroHoras> buscarPorEmpleado(Long empleadoId);
    void eliminarPorRegistroHoras(Long registroHorasId);
}

