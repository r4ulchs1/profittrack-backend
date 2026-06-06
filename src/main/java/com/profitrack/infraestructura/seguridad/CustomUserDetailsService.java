package com.profitrack.infraestructura.seguridad;

import com.profitrack.dominio.model.Administrador;
import com.profitrack.dominio.model.Duenio;
import com.profitrack.dominio.model.Empleado;
import com.profitrack.dominio.puerto.salida.AdministradorRepository;
import com.profitrack.dominio.puerto.salida.DuenioRepository;
import com.profitrack.dominio.puerto.salida.EmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmpleadoRepository empleadoRepository;
    private final DuenioRepository duenioRepository;
    private final AdministradorRepository administradorRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        // checkeamos primero si el q se loguea es un dev o pm
        Optional<Empleado> empleadoOpt = empleadoRepository.buscarPorCorreoYActivo(correo);
        if (empleadoOpt.isPresent()) {
            Empleado e = empleadoOpt.get();
            String rol = e.getRol() != null ? e.getRol().getNombre() : "SIN_ROL";
            return User.builder()
                    .username(e.getCorreo())
                    .password(e.getContrasenia())
                    .roles(rol)
                    .build();
        }

        // 2. Buscar en dueños
        Optional<Duenio> duenioOpt = duenioRepository.buscarPorCorreoYActivo(correo);
        if (duenioOpt.isPresent()) {
            Duenio d = duenioOpt.get();
            return User.builder()
                    .username(d.getCorreo())
                    .password(d.getContrasenia())
                    .roles("Owner")
                    .build();
        }

        // 3. Buscar en administradores
        Optional<Administrador> adminOpt = administradorRepository.buscarPorCorreoYActivo(correo);
        if (adminOpt.isPresent()) {
            Administrador a = adminOpt.get();
            return User.builder()
                    .username(a.getCorreo())
                    .password(a.getContrasenia())
                    .roles("Administrador")
                    .build();
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + correo);
    }
}
