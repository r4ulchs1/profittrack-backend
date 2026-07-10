package com.profitrack.aplicacion.puerto.entrada;

import com.profitrack.aplicacion.dto.empresaDto.EmpresaRequestDto;
import com.profitrack.aplicacion.dto.empresaDto.EmpresaResponseDto;
import com.profitrack.aplicacion.dto.empresaDto.EmpresaPatchDto;
import com.profitrack.aplicacion.dto.empleadoDto.EmpleadoRequestDto;
import com.profitrack.aplicacion.dto.empleadoDto.EmpleadoResponseDto;
import com.profitrack.aplicacion.dto.proyectoDto.ProyectoResponseDto;
import com.profitrack.aplicacion.dto.rolDto.RolResponseDto;
import java.util.List;
import java.util.Map;

public interface AdministradorSaasUseCase {
    List<EmpresaResponseDto> listarTodasLasEmpresas();
    EmpresaResponseDto obtenerEmpresaPorId(Long id);
    EmpresaResponseDto crearEmpresa(EmpresaRequestDto dto);
    EmpresaResponseDto actualizarEmpresa(Long id, EmpresaPatchDto dto);
    void eliminarEmpresa(Long id);
    EmpresaResponseDto cambiarEstadoEmpresa(Long id, boolean activo);
    
    List<EmpleadoResponseDto> listarEmpleadosPorEmpresa(Long empresaId);
    EmpleadoResponseDto crearEmpleadoParaEmpresa(Long empresaId, EmpleadoRequestDto dto);
    List<RolResponseDto> listarRolesPorEmpresa(Long empresaId);
    
    List<ProyectoResponseDto> listarProyectosPorEmpresa(Long empresaId);
    
    Map<String, Object> obtenerEstadisticasGlobales();
    Map<String, Object> obtenerPerfilAdmin(Long adminId);
}
