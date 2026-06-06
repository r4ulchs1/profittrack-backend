package com.profitrack.infraestructura.configuracion;

import com.profitrack.dominio.model.Administrador;
import com.profitrack.dominio.model.CategoriaEgreso;
import com.profitrack.dominio.model.Cliente;
import com.profitrack.dominio.model.CostoRegistroHoras;
import com.profitrack.dominio.model.Duenio;
import com.profitrack.dominio.model.Egreso;
import com.profitrack.dominio.model.Empleado;
import com.profitrack.dominio.model.Empresa;
import com.profitrack.dominio.model.EstadoEtapa;
import com.profitrack.dominio.model.EstadoProyecto;
import com.profitrack.dominio.model.EstadoTarea;
import com.profitrack.dominio.model.EtapaProyecto;
import com.profitrack.dominio.model.Ingreso;
import com.profitrack.dominio.model.MetricaProyecto;
import com.profitrack.dominio.model.Proyecto;
import com.profitrack.dominio.model.ProyectoCostoEmpleado;
import com.profitrack.dominio.model.ProyectoEmpleado;
import com.profitrack.dominio.model.RegistroHoras;
import com.profitrack.dominio.model.Rol;
import com.profitrack.dominio.model.TipoIngreso;
import com.profitrack.dominio.model.TipoServicio;
import com.profitrack.dominio.model.TipoTarea;
import com.profitrack.infraestructura.adaptador.salida.persistencia.repositorio.AdministradorJpaRepository;
import com.profitrack.infraestructura.repository.CategoriaEgresoJpaRepository;
import com.profitrack.infraestructura.repository.ClienteJpaRepository;
import com.profitrack.infraestructura.repository.CostoRegistroHorasJpaRepository;
import com.profitrack.infraestructura.repository.DuenioJpaRepository;
import com.profitrack.infraestructura.repository.EgresoJpaRepository;
import com.profitrack.infraestructura.repository.EmpleadoJpaRepository;
import com.profitrack.infraestructura.repository.EmpresaJpaRepository;
import com.profitrack.infraestructura.repository.EtapaProyectoJpaRepository;
import com.profitrack.infraestructura.repository.IngresoJpaRepository;
import com.profitrack.infraestructura.repository.MetricaProyectoJpaRepository;
import com.profitrack.infraestructura.repository.ProyectoCostoEmpleadoJpaRepository;
import com.profitrack.infraestructura.repository.ProyectoEmpleadoJpaRepository;
import com.profitrack.infraestructura.repository.ProyectoJpaRepository;
import com.profitrack.infraestructura.repository.RegistroHorasJpaRepository;
import com.profitrack.infraestructura.repository.RolJpaRepository;
import com.profitrack.infraestructura.repository.TareaProyectoJpaRepository;
import com.profitrack.infraestructura.repository.TipoServicioJpaRepository;
import com.profitrack.infraestructura.repository.TipoTareaJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

        private static final String DEMO_PROJECT_CODE = "PT-DEMO-OWNER";

        private final EmpresaJpaRepository empresaRepo;
        private final RolJpaRepository rolRepo;
        private final EmpleadoJpaRepository empleadoRepo;
        private final DuenioJpaRepository duenioRepo;
        private final AdministradorJpaRepository administradorRepo;
        private final ClienteJpaRepository clienteRepo;
        private final TipoServicioJpaRepository tipoServicioRepo;
        private final TipoTareaJpaRepository tipoTareaRepo;
        private final ProyectoJpaRepository proyectoRepo;
        private final EtapaProyectoJpaRepository etapaRepo;
        private final TareaProyectoJpaRepository tareaRepo;
        private final ProyectoEmpleadoJpaRepository proyectoEmpleadoRepo;
        private final ProyectoCostoEmpleadoJpaRepository proyectoCostoEmpleadoRepo;
        private final RegistroHorasJpaRepository registroHorasRepo;
        private final CostoRegistroHorasJpaRepository costoRegistroHorasRepo;
        private final IngresoJpaRepository ingresoRepo;
        private final EgresoJpaRepository egresoRepo;
        private final CategoriaEgresoJpaRepository categoriaEgresoRepo;
        private final MetricaProyectoJpaRepository metricaRepo;
        private final PasswordEncoder passwordEncoder;

        @Override
        @Transactional
        public void run(String... args) {
                log.info("Iniciando verificacion de datos demo...");

                Empresa empresa = obtenerOCrearEmpresa();
                Rol rolGerente = obtenerOCrearRol(empresa, "Gerente",
                                "Gerente general, revisa rentabilidad y reportes");
                Rol rolPM = obtenerOCrearRol(empresa, "PM",
                                "Project Manager, gestiona proyectos y aprueba horas");
                Rol rolAdmin = obtenerOCrearRol(empresa, "Administrador",
                                "Administrador operativo, registra empleados");
                Rol rolDev = obtenerOCrearRol(empresa, "Desarrollador",
                                "Desarrollador, reporta horas trabajadas");
                Rol rolQA = obtenerOCrearRol(empresa, "QA",
                                "QA, ejecuta pruebas y validaciones");

                obtenerOCrearDuenio(empresa);
                obtenerOCrearEmpleado(empresa, rolGerente, "gerente@techconsult.pe", "Maria", "Garcia Lopez",
                                "gerente123");
                Empleado pm = obtenerOCrearEmpleado(empresa, rolPM, "pm@techconsult.pe", "Juan", "Perez Sanchez",
                                "pm123");
                obtenerOCrearEmpleado(empresa, rolAdmin, "admin@techconsult.pe", "Ana", "Rodriguez Torres",
                                "admin123");
                Administrador admin = obtenerOCrearAdministrador("admin@profit.com", "Admin", "ProfiTrack",
                                "admin123");
                Empleado backend = obtenerOCrearEmpleado(empresa, rolDev, "dev@techconsult.pe", "Luis",
                                "Fernandez Diaz", "dev123");
                Empleado frontend = obtenerOCrearEmpleado(empresa, rolDev, "frontend@techconsult.pe", "Sofia",
                                "Vargas Rios", "frontend123");
                Empleado qa = obtenerOCrearEmpleado(empresa, rolQA, "qa@techconsult.pe", "Diego",
                                "Quispe Ramos", "qa123");

                Cliente cliente = obtenerOCrearCliente(empresa, "Banco Digital SAC", "20501234567",
                                "Roberto Gomez", "rgomez@bancodigital.pe");
                obtenerOCrearCliente(empresa, "Retail Cloud EIRL", "20607654321",
                                "Laura Mendoza", "lmendoza@retailcloud.pe");

                TipoServicio servicio = obtenerOCrearTipoServicio(empresa, "Desarrollo Web",
                                "Desarrollo de aplicaciones web full-stack");
                obtenerOCrearTipoServicio(empresa, "Desarrollo Mobile", "Desarrollo de apps moviles");
                obtenerOCrearTipoServicio(empresa, "Consultoria TI", "Asesoria en tecnologia de la informacion");

                TipoTarea backendTipo = obtenerOCrearTipoTarea(empresa, "Backend", "Servicios, API y base de datos");
                TipoTarea frontendTipo = obtenerOCrearTipoTarea(empresa, "Frontend",
                                "Interfaz y experiencia de usuario");
                TipoTarea qaTipo = obtenerOCrearTipoTarea(empresa, "QA", "Pruebas funcionales y despliegue");
                TipoTarea gestionTipo = obtenerOCrearTipoTarea(empresa, "Gestion",
                                "Analisis, coordinacion y seguimiento");

                CategoriaEgreso infraestructura = obtenerOCrearCategoria(empresa, "Infraestructura");
                CategoriaEgreso licencias = obtenerOCrearCategoria(empresa, "Licencias");
                CategoriaEgreso serviciosExternos = obtenerOCrearCategoria(empresa, "Servicios externos");

                if (proyectoDemoExiste(empresa.getId())) {
                        log.info("Proyecto demo {} ya existe, no se duplican datos.", DEMO_PROJECT_CODE);
                        return;
                }

                crearProyectoDemo(empresa, cliente, servicio, pm, backend, frontend, qa,
                                backendTipo, frontendTipo, qaTipo, gestionTipo,
                                infraestructura, licencias, serviciosExternos);

                log.info("Seed demo completado. Credenciales: owner@techconsult.pe / owner123, pm@techconsult.pe / pm123, admin@profit.com / admin123.");
        }

        private Empresa obtenerOCrearEmpresa() {
                return empresaRepo.findAll().stream()
                                .filter(e -> "20612345678".equals(e.getRuc()))
                                .findFirst()
                                .orElseGet(() -> empresaRepo.save(Empresa.builder()
                                                .nombre("TechConsult SAC")
                                                .ruc("20612345678")
                                                .direccion("Av. Javier Prado 1234, Lima")
                                                .telefono("01-234-5678")
                                                .correo("info@techconsult.pe")
                                                .build()));
        }

        private Rol obtenerOCrearRol(Empresa empresa, String nombre, String descripcion) {
                return rolRepo.findByEmpresaIdAndNombreIgnoreCase(empresa.getId(), nombre)
                                .orElseGet(() -> rolRepo.save(Rol.builder()
                                                .empresa(empresa)
                                                .nombre(nombre)
                                                .descripcion(descripcion)
                                                .build()));
        }

    private void obtenerOCrearDuenio(Empresa empresa) {
                duenioRepo.findAll().stream()
                                .filter(d -> "owner@techconsult.pe".equalsIgnoreCase(d.getCorreo()))
                                .findFirst()
                                .map(d -> {
                                        d.setEmpresa(empresa);
                                        d.setActivo(true);
                                        if (d.getContrasenia() == null) {
                                                d.setContrasenia(passwordEncoder.encode("owner123"));
                                        }
                                        return duenioRepo.save(d);
                                })
                                .orElseGet(() -> duenioRepo.save(Duenio.builder()
                                                .empresa(empresa)
                                                .nombres("Carlos")
                                                .apellidos("Rivera Mendoza")
                                                .correo("owner@techconsult.pe")
                                                .contrasenia(passwordEncoder.encode("owner123"))
                                                .build()));
        }

        private Administrador obtenerOCrearAdministrador(String correo, String nombres, String apellidos, String contrasenia) {
                return administradorRepo.findAll().stream()
                                .filter(a -> correo.equalsIgnoreCase(a.getCorreo()))
                                .findFirst()
                                .map(a -> {
                                        a.setActivo(true);
                                        if (a.getContrasenia() == null) {
                                                a.setContrasenia(passwordEncoder.encode(contrasenia));
                                        }
                                        return administradorRepo.save(a);
                                })
                                .orElseGet(() -> administradorRepo.save(Administrador.builder()
                                                .correo(correo)
                                                .nombres(nombres)
                                                .apellidos(apellidos)
                                                .contrasenia(passwordEncoder.encode(contrasenia))
                                                .build()));
        }

        private Empleado obtenerOCrearEmpleado(Empresa empresa, Rol rol, String correo, String nombres,
                        String apellidos, String contrasenia) {
                return empleadoRepo.findAll().stream()
                                .filter(e -> correo.equalsIgnoreCase(e.getCorreo()))
                                .findFirst()
                                .map(e -> {
                                        e.setEmpresa(empresa);
                                        e.setRol(rol);
                                        e.setNombres(e.getNombres() != null ? e.getNombres() : nombres);
                                        e.setApellidos(e.getApellidos() != null ? e.getApellidos() : apellidos);
                                        e.setActivo(true);
                                        if (e.getContrasenia() == null) {
                                                e.setContrasenia(passwordEncoder.encode(contrasenia));
                                        }
                                        if (e.getFechaIngreso() == null) {
                                                e.setFechaIngreso(LocalDate.now().minusMonths(4));
                                        }
                                        return empleadoRepo.save(e);
                                })
                                .orElseGet(() -> empleadoRepo.save(Empleado.builder()
                                                .empresa(empresa)
                                                .rol(rol)
                                                .nombres(nombres)
                                                .apellidos(apellidos)
                                                .correo(correo)
                                                .contrasenia(passwordEncoder.encode(contrasenia))
                                                .fechaIngreso(LocalDate.now().minusMonths(4))
                                                .build()));
        }

        private Cliente obtenerOCrearCliente(Empresa empresa, String razonSocial, String ruc,
                        String contacto, String correoContacto) {
                return clienteRepo.findAllByEmpresaIdAndActivoTrue(empresa.getId()).stream()
                                .filter(c -> ruc.equals(c.getRuc()))
                                .findFirst()
                                .orElseGet(() -> clienteRepo.save(Cliente.builder()
                                                .empresa(empresa)
                                                .razonSocial(razonSocial)
                                                .ruc(ruc)
                                                .nombreContacto(contacto)
                                                .correoContacto(correoContacto)
                                                .telefonoContacto("01-555-1234")
                                                .direccion("Lima")
                                                .build()));
        }

        private TipoServicio obtenerOCrearTipoServicio(Empresa empresa, String nombre, String descripcion) {
                return tipoServicioRepo.findAllByEmpresaIdAndActivoTrue(empresa.getId()).stream()
                                .filter(t -> nombre.equalsIgnoreCase(t.getNombre()))
                                .findFirst()
                                .orElseGet(() -> tipoServicioRepo.save(TipoServicio.builder()
                                                .empresa(empresa)
                                                .nombre(nombre)
                                                .descripcion(descripcion)
                                                .build()));
        }

        private TipoTarea obtenerOCrearTipoTarea(Empresa empresa, String nombre, String descripcion) {
                return tipoTareaRepo.findAllByEmpresaIdAndActivoTrue(empresa.getId()).stream()
                                .filter(t -> nombre.equalsIgnoreCase(t.getNombre()))
                                .findFirst()
                                .orElseGet(() -> tipoTareaRepo.save(TipoTarea.builder()
                                                .empresa(empresa)
                                                .nombre(nombre)
                                                .descripcion(descripcion)
                                                .build()));
        }

        private CategoriaEgreso obtenerOCrearCategoria(Empresa empresa, String nombre) {
                return categoriaEgresoRepo.findAllByEmpresaId(empresa.getId()).stream()
                                .filter(c -> nombre.equalsIgnoreCase(c.getNombre()))
                                .findFirst()
                                .orElseGet(() -> categoriaEgresoRepo.save(CategoriaEgreso.builder()
                                                .empresa(empresa)
                                                .nombre(nombre)
                                                .build()));
        }

        private boolean proyectoDemoExiste(Long empresaId) {
                return proyectoRepo.findAllByEmpresaIdAndActivoTrue(empresaId).stream()
                                .anyMatch(p -> DEMO_PROJECT_CODE.equalsIgnoreCase(p.getCodigo()));
        }

        private void crearProyectoDemo(Empresa empresa, Cliente cliente, TipoServicio servicio, Empleado pm,
                        Empleado backend, Empleado frontend, Empleado qa, TipoTarea backendTipo,
                        TipoTarea frontendTipo, TipoTarea qaTipo, TipoTarea gestionTipo,
                        CategoriaEgreso infraestructura, CategoriaEgreso licencias,
                        CategoriaEgreso serviciosExternos) {

                LocalDate hoy = LocalDate.now();
                LocalDate inicio = hoy.minusDays(35);

                Proyecto proyecto = proyectoRepo.save(Proyecto.builder()
                                .empresa(empresa)
                                .cliente(cliente)
                                .tipoServicio(servicio)
                                .liderEmpleado(pm)
                                .codigo(DEMO_PROJECT_CODE)
                                .nombre("Plataforma Ecommerce B2B")
                                .descripcion("Proyecto demo avanzado para revisar metricas owner, horas, costos e ingresos.")
                                .fechaInicioPlanificada(inicio.minusDays(3))
                                .fechaFinPlanificada(hoy.plusDays(45))
                                .fechaInicioReal(inicio)
                                .horasPlanificadas(bd("240.00"))
                                .horasReales(bd("145.50"))
                                .presupuestoPlanificado(bd("27000.00"))
                                .costoReal(bd("11657.50"))
                                .margenPlanificado(bd("15000.00"))
                                .margenReal(bd("15342.50"))
                                .precioVenta(bd("42000.00"))
                                .estado(EstadoProyecto.EN_PROCESO)
                                .build());

                asignarEmpleado(proyecto, pm, "Project Manager", bd("65.00"), inicio);
                asignarEmpleado(proyecto, backend, "Backend Developer", bd("45.00"), inicio);
                asignarEmpleado(proyecto, frontend, "Frontend Developer", bd("42.00"), inicio.plusDays(3));
                asignarEmpleado(proyecto, qa, "QA Analyst", bd("38.00"), inicio.plusDays(20));

                EtapaProyecto analisis = guardarEtapa(proyecto, "Descubrimiento y analisis", 1,
                                EstadoEtapa.FINALIZADA, bd("40.00"), bd("38.00"),
                                inicio.minusDays(3), inicio.plusDays(8), inicio, inicio.plusDays(8));
                EtapaProyecto desarrollo = guardarEtapa(proyecto, "Desarrollo MVP", 2,
                                EstadoEtapa.EN_CURSO, bd("140.00"), bd("107.50"),
                                inicio.plusDays(9), hoy.plusDays(20), inicio.plusDays(9), null);
                EtapaProyecto qaEtapa = guardarEtapa(proyecto, "QA y despliegue", 3,
                                EstadoEtapa.PENDIENTE, bd("60.00"), bd("0.00"),
                                hoy.plusDays(21), hoy.plusDays(45), null, null);

                var req = guardarTarea(proyecto, analisis, gestionTipo, pm,
                                "Levantamiento de requerimientos", EstadoTarea.FINALIZADO,
                                bd("16.00"), bd("15.00"), inicio, inicio.plusDays(4), inicio, inicio.plusDays(4));
                var funcional = guardarTarea(proyecto, analisis, gestionTipo, pm,
                                "Diseno funcional y alcance", EstadoTarea.FINALIZADO,
                                bd("24.00"), bd("23.00"), inicio.plusDays(5), inicio.plusDays(8),
                                inicio.plusDays(5), inicio.plusDays(8));
                var login = guardarTarea(proyecto, desarrollo, backendTipo, backend,
                                "Login con JWT y refresh token", EstadoTarea.FINALIZADO,
                                bd("18.00"), bd("18.00"), inicio.plusDays(9), inicio.plusDays(13),
                                inicio.plusDays(9), inicio.plusDays(13));
                var productos = guardarTarea(proyecto, desarrollo, backendTipo, backend,
                                "CRUD de productos y categorias", EstadoTarea.FINALIZADO,
                                bd("26.00"), bd("24.00"), inicio.plusDays(14), inicio.plusDays(20),
                                inicio.plusDays(14), inicio.plusDays(20));
                var checkout = guardarTarea(proyecto, desarrollo, backendTipo, backend,
                                "Carrito y checkout", EstadoTarea.EN_CURSO,
                                bd("40.00"), bd("27.50"), inicio.plusDays(21), hoy.plusDays(8),
                                inicio.plusDays(21), null);
                var panel = guardarTarea(proyecto, desarrollo, frontendTipo, frontend,
                                "Panel administrativo responsive", EstadoTarea.EN_CURSO,
                                bd("32.00"), bd("14.00"), inicio.plusDays(17), hoy.plusDays(10),
                                inicio.plusDays(17), null);
                guardarTarea(proyecto, desarrollo, backendTipo, backend,
                                "Integracion de pasarela de pagos", EstadoTarea.PENDIENTE,
                                bd("24.00"), bd("0.00"), hoy.plusDays(3), hoy.plusDays(15), null, null);
                guardarTarea(proyecto, qaEtapa, qaTipo, qa,
                                "Pruebas integrales", EstadoTarea.PENDIENTE,
                                bd("35.00"), bd("0.00"), hoy.plusDays(21), hoy.plusDays(35), null, null);
                guardarTarea(proyecto, qaEtapa, qaTipo, qa,
                                "Despliegue a produccion", EstadoTarea.PENDIENTE,
                                bd("25.00"), bd("0.00"), hoy.plusDays(36), hoy.plusDays(45), null, null);

                registrarHoras(proyecto, req, pm, inicio.plusDays(1), bd("7.50"), true, bd("65.00"),
                                "Kickoff y entrevistas con stakeholders");
                registrarHoras(proyecto, req, pm, inicio.plusDays(2), bd("7.50"), true, bd("65.00"),
                                "Mapa de procesos y backlog inicial");
                registrarHoras(proyecto, funcional, pm, inicio.plusDays(6), bd("8.00"), true, bd("65.00"),
                                "Definicion de historias y criterios");
                registrarHoras(proyecto, funcional, pm, inicio.plusDays(7), bd("8.00"), true, bd("65.00"),
                                "Priorizacion con cliente");
                registrarHoras(proyecto, funcional, pm, inicio.plusDays(8), bd("7.00"), true, bd("65.00"),
                                "Cierre de alcance MVP");

                registrarHoras(proyecto, login, backend, inicio.plusDays(10), bd("8.00"), true, bd("45.00"),
                                "Implementacion access token");
                registrarHoras(proyecto, login, backend, inicio.plusDays(11), bd("8.00"), true, bd("45.00"),
                                "Refresh token y seguridad");
                registrarHoras(proyecto, login, backend, inicio.plusDays(12), bd("2.00"), true, bd("45.00"),
                                "Ajustes y pruebas de login");

                registrarHoras(proyecto, productos, backend, inicio.plusDays(15), bd("8.00"), true, bd("45.00"),
                                "Modelo y endpoints de productos");
                registrarHoras(proyecto, productos, backend, inicio.plusDays(16), bd("8.00"), true, bd("45.00"),
                                "Categorias y validaciones");
                registrarHoras(proyecto, productos, backend, inicio.plusDays(18), bd("8.00"), true, bd("45.00"),
                                "Correcciones y pruebas API");

                registrarHoras(proyecto, checkout, backend, inicio.plusDays(23), bd("8.00"), true, bd("45.00"),
                                "Carrito persistente");
                registrarHoras(proyecto, checkout, backend, inicio.plusDays(24), bd("8.00"), true, bd("45.00"),
                                "Calculo de totales");
                registrarHoras(proyecto, checkout, backend, inicio.plusDays(25), bd("8.00"), true, bd("45.00"),
                                "Checkout inicial");
                registrarHoras(proyecto, checkout, backend, hoy.minusDays(2), bd("3.50"), true, bd("45.00"),
                                "Ajustes por feedback");
                registrarHoras(proyecto, checkout, backend, hoy.minusDays(1), bd("4.00"), false, bd("45.00"),
                                "Pendiente de aprobacion PM");

                registrarHoras(proyecto, panel, frontend, inicio.plusDays(19), bd("7.00"), true, bd("42.00"),
                                "Layout dashboard admin");
                registrarHoras(proyecto, panel, frontend, inicio.plusDays(22), bd("7.00"), true, bd("42.00"),
                                "Componentes de tabla y filtros");

                ingresoRepo.save(Ingreso.builder()
                                .empresa(empresa)
                                .proyecto(proyecto)
                                .tipo(TipoIngreso.PAGO_PROYECTO)
                                .monto(bd("18000.00"))
                                .fechaIngreso(inicio.plusDays(2))
                                .descripcion("Primer hito facturado")
                                .build());
                ingresoRepo.save(Ingreso.builder()
                                .empresa(empresa)
                                .proyecto(proyecto)
                                .tipo(TipoIngreso.PAGO_PROYECTO)
                                .monto(bd("9000.00"))
                                .fechaIngreso(hoy.minusDays(5))
                                .descripcion("Segundo pago parcial")
                                .build());

                egresoRepo.save(Egreso.builder()
                                .empresa(empresa)
                                .proyecto(proyecto)
                                .categoria(infraestructura)
                                .monto(bd("1500.00"))
                                .fechaEgreso(inicio.plusDays(10))
                                .descripcion("Ambiente cloud para desarrollo")
                                .build());
                egresoRepo.save(Egreso.builder()
                                .empresa(empresa)
                                .proyecto(proyecto)
                                .categoria(licencias)
                                .monto(bd("1200.00"))
                                .fechaEgreso(inicio.plusDays(12))
                                .descripcion("Licencias de herramientas de diseno y testing")
                                .build());
                egresoRepo.save(Egreso.builder()
                                .empresa(empresa)
                                .proyecto(proyecto)
                                .categoria(serviciosExternos)
                                .monto(bd("3000.00"))
                                .fechaEgreso(hoy.minusDays(3))
                                .descripcion("Consultoria externa en arquitectura")
                                .build());

                metricaRepo.save(MetricaProyecto.builder()
                                .proyecto(proyecto)
                                .fechaSnapshot(hoy.minusDays(14))
                                .costoPlanificado(bd("27000.00"))
                                .costoReal(bd("6400.00"))
                                .ingresoPlanificado(bd("42000.00"))
                                .ingresoReal(bd("18000.00"))
                                .margenPlanificado(bd("15000.00"))
                                .margenReal(bd("11600.00"))
                                .horasPlanificadas(bd("240.00"))
                                .horasReales(bd("86.00"))
                                .build());
                metricaRepo.save(MetricaProyecto.builder()
                                .proyecto(proyecto)
                                .fechaSnapshot(hoy)
                                .costoPlanificado(bd("27000.00"))
                                .costoReal(bd("11657.50"))
                                .ingresoPlanificado(bd("42000.00"))
                                .ingresoReal(bd("27000.00"))
                                .margenPlanificado(bd("15000.00"))
                                .margenReal(bd("15342.50"))
                                .horasPlanificadas(bd("240.00"))
                                .horasReales(bd("145.50"))
                                .build());
        }

        private void asignarEmpleado(Proyecto proyecto, Empleado empleado, String rolAsignado, BigDecimal costoHora,
                        LocalDate fechaInicio) {
                proyectoEmpleadoRepo.save(ProyectoEmpleado.builder()
                                .proyecto(proyecto)
                                .empleado(empleado)
                                .rolAsignado(rolAsignado)
                                .fechaAsignacion(fechaInicio.atStartOfDay().toInstant(java.time.ZoneOffset.UTC))
                                .activo(true)
                                .build());

                proyectoCostoEmpleadoRepo.save(ProyectoCostoEmpleado.builder()
                                .proyecto(proyecto)
                                .empleado(empleado)
                                .costoHora(costoHora)
                                .fechaInicio(fechaInicio)
                                .fechaFin(null)
                                .build());
        }

        private EtapaProyecto guardarEtapa(Proyecto proyecto, String nombre, Integer orden, EstadoEtapa estado,
                        BigDecimal horasPlanificadas, BigDecimal horasReales,
                        LocalDate inicioPlan, LocalDate finPlan, LocalDate inicioReal, LocalDate finReal) {
                return etapaRepo.save(EtapaProyecto.builder()
                                .proyecto(proyecto)
                                .nombre(nombre)
                                .descripcion("Etapa demo: " + nombre)
                                .orden(orden)
                                .estado(estado)
                                .horasPlanificadas(horasPlanificadas)
                                .horasReales(horasReales)
                                .fechaInicioPlanificada(inicioPlan)
                                .fechaFinPlanificada(finPlan)
                                .fechaInicioReal(inicioReal)
                                .fechaFinReal(finReal)
                                .build());
        }

        private com.profitrack.dominio.model.TareaProyecto guardarTarea(Proyecto proyecto, EtapaProyecto etapa,
                        TipoTarea tipo, Empleado empleado, String nombre, EstadoTarea estado,
                        BigDecimal horasPlanificadas, BigDecimal horasReales,
                        LocalDate inicioPlan, LocalDate finPlan, LocalDate inicioReal, LocalDate finReal) {
                return tareaRepo.save(com.profitrack.dominio.model.TareaProyecto.builder()
                                .proyecto(proyecto)
                                .etapaProyecto(etapa)
                                .tipoTarea(tipo)
                                .empleadoAsignado(empleado)
                                .nombre(nombre)
                                .descripcion("Tarea demo para metricas owner")
                                .estado(estado)
                                .horasPlanificadas(horasPlanificadas)
                                .horasReales(horasReales)
                                .fechaInicioPlanificada(inicioPlan)
                                .fechaFinPlanificada(finPlan)
                                .fechaInicioReal(inicioReal)
                                .fechaFinReal(finReal)
                                .build());
        }

        private void registrarHoras(Proyecto proyecto, com.profitrack.dominio.model.TareaProyecto tarea,
                        Empleado empleado, LocalDate fecha, BigDecimal horas, boolean aprobado,
                        BigDecimal costoHora, String descripcion) {
                LocalDateTime ingreso = fecha.atTime(9, 0);
                RegistroHoras registro = registroHorasRepo.save(RegistroHoras.builder()
                                .empleado(empleado)
                                .proyecto(proyecto)
                                .tarea(tarea)
                                .fechaTrabajo(fecha)
                                .horaIngreso(ingreso)
                                .horaSalida(ingreso.plusHours(8))
                                .minutosDescanso(60)
                                .horasTrabajadas(horas)
                                .descripcion(descripcion)
                                .aprobado(aprobado)
                                .build());

                if (aprobado) {
                        costoRegistroHorasRepo.save(CostoRegistroHoras.builder()
                                        .registroHoras(registro)
                                        .costoHora(costoHora)
                                        .costoTotal(costoHora.multiply(horas))
                                        .fechaCalculo(Instant.now())
                                        .build());
                }
        }

        private BigDecimal bd(String value) {
                return new BigDecimal(value);
        }
}
