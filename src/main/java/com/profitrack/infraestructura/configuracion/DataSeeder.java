package com.profitrack.infraestructura.configuracion;

import com.profitrack.dominio.model.*;
import com.profitrack.infraestructura.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

//Datos de pruieba
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

        private final EmpresaJpaRepository empresaRepo;
        private final RolJpaRepository rolRepo;
        private final EmpleadoJpaRepository empleadoRepo;
        private final DuenioJpaRepository duenioRepo;
        private final ClienteJpaRepository clienteRepo;
        private final TipoServicioJpaRepository tipoServicioRepo;
        private final PasswordEncoder passwordEncoder;

        @Override
        public void run(String... args) {
                if (empresaRepo.count() > 0) {
                        log.info("Datos ya existentes — seeder omitido.");
                        return;
                }

                log.info("🌱 Iniciando seed de datos...");

                // ── 1. Empresa ──
                Empresa empresa = empresaRepo.save(Empresa.builder()
                                .nombre("TechConsult SAC")
                                .ruc("20612345678")
                                .direccion("Av. Javier Prado 1234, Lima")
                                .telefono("01-234-5678")
                                .correo("info@techconsult.pe")
                                .build());

                // ── 2. Roles ──
                Rol rolGerente = rolRepo.save(Rol.builder().empresa(empresa).nombre("Gerente")
                                .descripcion("Gerente general — visualiza rentabilidad y reportes").build());
                Rol rolPM = rolRepo.save(Rol.builder().empresa(empresa).nombre("PM")
                                .descripcion("Project Manager — gestiona proyectos").build());
                Rol rolAdmin = rolRepo.save(Rol.builder().empresa(empresa).nombre("Administrador")
                                .descripcion("Administrador operativo — registra empleados").build());
                Rol rolDev = rolRepo.save(Rol.builder().empresa(empresa).nombre("Desarrollador")
                                .descripcion("Desarrollador — reporta horas trabajadas").build());

                // ── 3. Owner (Dueño) ──
                duenioRepo.save(Duenio.builder()
                                .empresa(empresa)
                                .nombres("Carlos")
                                .apellidos("Rivera Mendoza")
                                .correo("owner@techconsult.pe")
                                .contrasenia(passwordEncoder.encode("owner123"))
                                .build());

                // ── 4. Empleados de prueba ──
                empleadoRepo.save(Empleado.builder()
                                .empresa(empresa).rol(rolGerente)
                                .nombres("María").apellidos("García López")
                                .correo("gerente@techconsult.pe")
                                .contrasenia(passwordEncoder.encode("gerente123"))
                                .build());

                empleadoRepo.save(Empleado.builder()
                                .empresa(empresa).rol(rolPM)
                                .nombres("Juan").apellidos("Pérez Sánchez")
                                .correo("pm@techconsult.pe")
                                .contrasenia(passwordEncoder.encode("pm123"))
                                .build());

                empleadoRepo.save(Empleado.builder()
                                .empresa(empresa).rol(rolAdmin)
                                .nombres("Ana").apellidos("Rodríguez Torres")
                                .correo("admin@techconsult.pe")
                                .contrasenia(passwordEncoder.encode("admin123"))
                                .build());

                empleadoRepo.save(Empleado.builder()
                                .empresa(empresa).rol(rolDev)
                                .nombres("Luis").apellidos("Fernández Díaz")
                                .correo("dev@techconsult.pe")
                                .contrasenia(passwordEncoder.encode("dev123"))
                                .build());

                // ── 5. Clientes ──
                clienteRepo.save(Cliente.builder()
                                .empresa(empresa)
                                .razonSocial("Banco Digital SAC")
                                .ruc("20501234567")
                                .nombreContacto("Roberto Gómez")
                                .correoContacto("rgomez@bancodigital.pe")
                                .telefonoContacto("01-987-6543")
                                .direccion("Av. Larco 456, Miraflores")
                                .build());

                clienteRepo.save(Cliente.builder()
                                .empresa(empresa)
                                .razonSocial("Retail Cloud EIRL")
                                .ruc("20607654321")
                                .nombreContacto("Laura Mendoza")
                                .correoContacto("lmendoza@retailcloud.pe")
                                .telefonoContacto("01-555-1234")
                                .direccion("Jr. Huallaga 789, Lima")
                                .build());

                // ── 6. Tipos de servicio ──
                tipoServicioRepo.save(TipoServicio.builder()
                                .empresa(empresa).nombre("Desarrollo Web")
                                .descripcion("Desarrollo de aplicaciones web full-stack").build());
                tipoServicioRepo.save(TipoServicio.builder()
                                .empresa(empresa).nombre("Desarrollo Mobile")
                                .descripcion("Desarrollo de apps móviles").build());
                tipoServicioRepo.save(TipoServicio.builder()
                                .empresa(empresa).nombre("Consultoría TI")
                                .descripcion("Asesoría en tecnología de la información").build());

                log.info("🎉 Seed completado: 1 empresa, 4 roles, 1 owner, 4 empleados, 2 clientes, 3 tipos servicio");
        }
}
