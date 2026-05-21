# Paso a paso del Flujo de Trabajo

El flujo general del sistema es correcto y está completamente alineado con la lógica de negocio implementada en el backend. A continuación se detallan los endpoints exactos y los pasos del flujo con sus respectivas rutas del backend:

## 1. Configuración Inicial (Owner / Gerente / PM)
El Owner (o el Gerente/PM según corresponda) inicializa los datos maestros de la empresa:
- `POST /api/empleados` - Registro de colaboradores.
- `POST /api/clientes` - Registro de clientes.
- `POST /api/tipos-servicio` - Definición de tipos de servicio.
- `POST /api/tipos-tarea` - Definición de tipos de tareas (análisis, desarrollo, QA, etc.).
- `POST /api/planilla` - Definición del sueldo base mensual del empleado, lo que calcula automáticamente su costo hora estimado (sueldo / 160h) y lo registra en el historial.
- `POST /api/historial-costo-hora` - Definición o actualización manual de la tarifa costo/hora de un colaborador, propagándose a sus proyectos activos.

## 2. Creación y Asignación de Proyecto
- `POST /api/proyectos` - Registro del proyecto (con sus fechas estimadas y presupuesto).
- `POST /api/proyecto-empleados` - Asignación de un colaborador a un proyecto con una tarifa específica (se inicializa con la tarifa activa del colaborador).
- `POST /api/tareas` - Creación de tareas específicas para el proyecto asignadas a los colaboradores.

## 3. Registro de Horas (Empleado)
El colaborador trabaja en el proyecto y registra su tiempo en su hoja de horas (timesheet):
- `GET /api/tareas/proyecto/{id}` - El colaborador consulta las tareas asignadas para el proyecto.
- `POST /api/registro-horas` - Registro diario del tiempo dedicado, indicando el proyecto, la tarea y las horas trabajadas.
- `GET /api/registro-horas/mis-horas` - Listado para que el colaborador consulte sus registros de horas.

## 4. Aprobación y Costos (Owner / Gerente / PM)
- `PATCH /api/registro-horas/{id}/aprobar` - El Owner/PM/Gerente aprueba el registro de horas del colaborador.
  - **Lógica interna:** Al aprobar, el sistema busca la tarifa activa del empleado para ese proyecto (`proyecto_costo_empleado`) y calcula el costo laboral correspondiente (`horasTrabajadas * costoHora`), almacenándolo en `costo_registro_horas`.
- `PATCH /api/registro-horas/{id}/rechazar` - Si el registro es incorrecto, puede ser rechazado.

## 5. Visualización de KPIS y Rentabilidad
El Owner/Gerente/PM visualiza el estado y rentabilidad en tiempo real:
- `GET /api/metricas/proyecto/{id}/actual` - Devuelve las métricas de rentabilidad actuales en tiempo real (Ingresos vs Costos [Laborales + OPEX], Margen de Ganancia, SPI, CPI y si el proyecto sigue siendo rentable).
- `POST /api/metricas/proyecto/{id}/snapshot` - Registra una captura histórica del estado del proyecto para trazabilidad en el tiempo.
- `GET /api/metricas/proyecto/{id}` - Listado de snapshots históricos de métricas.