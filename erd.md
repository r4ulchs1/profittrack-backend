// ======================================================
// SISTEMA HH.HH - GESTIÓN DE PROYECTOS
// ======================================================

Project sistema_hh_hh {
database_type: "PostgreSQL"

Note: '''
Sistema de gestión de proyectos basado en Horas Hombre (HH.HH)
'''
}

// ======================================================
// EMPRESAS
// ======================================================

Table empresas {
id bigint [pk, increment]

nombre varchar(200) [not null]
ruc varchar(20)

direccion varchar(255)
telefono varchar(30)
correo varchar(120)

created_at timestamp
updated_at timestamp
}

// ======================================================
// OWNERS / DUEÑOS
// ======================================================

Table owners {
id bigint [pk, increment]

empresa_id bigint [not null, ref: > empresas.id]

nombres varchar(120)
apellidos varchar(120)

correo varchar(120) [unique]
contrasenia varchar(255)

activo boolean [default: true]

created_at timestamp
updated_at timestamp
}

// ======================================================
// ADMINISTRADORES DEL SISTEMA
// ======================================================

Table administradores {
id bigint [pk, increment]

nombres varchar(120)
apellidos varchar(120)

correo varchar(120) [unique]
contrasenia varchar(255)

created_at timestamp
updated_at timestamp
}

// ======================================================
// ROLES
// ======================================================

Table roles {
id bigint [pk, increment]

empresa_id bigint [ref: > empresas.id]

nombre varchar(100) [not null]
descripcion text

created_at timestamp
}

// ======================================================
// EMPLEADOS
// ======================================================

Table empleados {
id bigint [pk, increment]

empresa_id bigint [not null, ref: > empresas.id]
rol_id bigint [ref: > roles.id]

nombres varchar(120)
apellidos varchar(120)

numero_documento varchar(30)

correo varchar(120) [unique]
telefono varchar(30)

contrasenia varchar(255)

fecha_ingreso date
fecha_salida date

activo boolean [default: true]

created_at timestamp
updated_at timestamp
}

// ======================================================
// TIPOS DE SERVICIOS
// ======================================================

Table tipos_servicio {
id bigint [pk, increment]

empresa_id bigint [not null, ref: > empresas.id]

nombre varchar(120)
descripcion text

created_at timestamp
}

// ======================================================
// CLIENTES
// ======================================================

Table clientes {
id bigint [pk, increment]

empresa_id bigint [not null, ref: > empresas.id]

razon_social varchar(200) [not null]
ruc varchar(20)

nombre_contacto varchar(120)
correo_contacto varchar(120)
telefono_contacto varchar(30)

direccion varchar(255)

created_at timestamp
updated_at timestamp
}

// ======================================================
// PROYECTOS
// ======================================================

Table proyectos {
id bigint [pk, increment]

empresa_id bigint [not null, ref: > empresas.id]
cliente_id bigint [ref: > clientes.id]
tipo_servicio_id bigint [not null, ref: > tipos_servicio.id]

lider_empleado_id bigint [ref: > empleados.id]

codigo varchar(50)

nombre varchar(200) [not null]
descripcion text

fecha_inicio_planificada date
fecha_fin_planificada date

fecha_inicio_real date
fecha_fin_real date

horas_planificadas decimal(10,2)
horas_reales decimal(10,2)

presupuesto_planificado decimal(14,2)
costo_real decimal(14,2)

margen_planificado decimal(14,2)
margen_real decimal(14,2)

precio_venta decimal(14,2)

estado varchar(50)
// PLANIFICADO
// EN_PROCESO
// PAUSADO
// FINALIZADO
// CANCELADO

created_at timestamp
updated_at timestamp
}

// ======================================================
// ETAPAS DEL PROYECTO
// ======================================================

Table etapas_proyecto {
id bigint [pk, increment]

proyecto_id bigint [not null, ref: > proyectos.id]

nombre varchar(160) [not null]
descripcion text

orden int [not null]

horas_planificadas decimal(10,2)
horas_reales decimal(10,2)

fecha_inicio_planificada date
fecha_fin_planificada date

fecha_inicio_real date
fecha_fin_real date

estado varchar(50)
// PENDIENTE
// EN_CURSO
// FINALIZADA

activo boolean [default: true]

created_at timestamp
updated_at timestamp
}

// ======================================================
// EMPLEADOS ASIGNADOS AL PROYECTO
// ======================================================

Table proyecto_empleados {
id bigint [pk, increment]

proyecto_id bigint [not null, ref: > proyectos.id]
empleado_id bigint [not null, ref: > empleados.id]

rol_asignado varchar(100)

fecha_asignacion timestamp
fecha_remocion timestamp

activo boolean [default: true]
}

// ======================================================
// HISTÓRICO COSTO HORA HOMBRE
// ======================================================

Table historial_costo_hora_empleado {
id bigint [pk, increment]

empleado_id bigint [not null, ref: > empleados.id]

costo_hora decimal(12,2)

fecha_inicio date
fecha_fin date

created_at timestamp
}

// ======================================================
// COSTO HORA POR PROYECTO
// ======================================================

Table proyecto_costo_empleado {
id bigint [pk, increment]

proyecto_id bigint [not null, ref: > proyectos.id]
empleado_id bigint [not null, ref: > empleados.id]

costo_hora decimal(12,2)

fecha_inicio date
fecha_fin date

created_at timestamp
}

// ======================================================
// TIPOS DE TAREAS
// ======================================================

Table tipos_tarea {
id bigint [pk, increment]

empresa_id bigint [not null, ref: > empresas.id]

nombre varchar(120)
descripcion text

created_at timestamp
}

// ======================================================
// TAREAS DEL PROYECTO
// ======================================================

Table tareas_proyecto {
id bigint [pk, increment]

proyecto_id bigint [not null, ref: > proyectos.id]
etapa_proyecto_id bigint [ref: > etapas_proyecto.id]
tipo_tarea_id bigint [ref: > tipos_tarea.id]

empleado_asignado_id bigint [ref: > empleados.id]

nombre varchar(200)
descripcion text

horas_planificadas decimal(10,2)
horas_reales decimal(10,2)

fecha_inicio_planificada date
fecha_fin_planificada date

fecha_inicio_real date
fecha_fin_real date

estado varchar(50)

created_at timestamp
updated_at timestamp
}

// ======================================================
// REGISTRO DE HORAS TRABAJADAS
// ======================================================

Table registro_horas {
id bigint [pk, increment]

empleado_id bigint [not null, ref: > empleados.id]
proyecto_id bigint [not null, ref: > proyectos.id]

tarea_id bigint [ref: > tareas_proyecto.id]

fecha_trabajo date

hora_ingreso timestamp
hora_salida timestamp

minutos_descanso int [default: 0]

horas_trabajadas decimal(10,2)

descripcion text

aprobado boolean [default: false]

created_at timestamp
}

// ======================================================
// COSTOS GENERADOS POR HH
// ======================================================

Table costos_registro_horas {
id bigint [pk, increment]

registro_horas_id bigint [not null, ref: > registro_horas.id]

costo_hora decimal(12,2)
costo_total decimal(12,2)

fecha_calculo timestamp
}

// ======================================================
// INGRESOS
// ======================================================

Table ingresos {
id bigint [pk, increment]

empresa_id bigint [not null, ref: > empresas.id]
proyecto_id bigint [ref: > proyectos.id]

tipo varchar(50)
// PAGO_PROYECTO
// SERVICIO_EXTRA
// OTRO

monto decimal(14,2)

fecha_ingreso date

descripcion text

created_at timestamp
}

// ======================================================
// CATEGORÍAS DE EGRESOS
// ======================================================

Table categorias_egreso {
id bigint [pk, increment]

empresa_id bigint [not null, ref: > empresas.id]

nombre varchar(100)

created_at timestamp
}

// ======================================================
// EGRESOS
// ======================================================

Table egresos {
id bigint [pk, increment]

empresa_id bigint [not null, ref: > empresas.id]
proyecto_id bigint [ref: > proyectos.id]

categoria_id bigint [ref: > categorias_egreso.id]

monto decimal(14,2)

fecha_egreso date

descripcion text

created_at timestamp
}

// ======================================================
// PLANILLAS
// ======================================================

Table planillas {
id bigint [pk, increment]

empresa_id bigint [not null, ref: > empresas.id]

anio int
mes int

monto_total decimal(14,2)

created_at timestamp
}

// ======================================================
// DETALLE PLANILLAS
// ======================================================

Table detalle_planillas {
id bigint [pk, increment]

planilla_id bigint [not null, ref: > planillas.id]
empleado_id bigint [not null, ref: > empleados.id]

sueldo_base decimal(14,2)

bonos decimal(14,2)
descuentos decimal(14,2)

sueldo_final decimal(14,2)

created_at timestamp
}

// ======================================================
// MÉTRICAS DEL PROYECTO
// ======================================================

Table metricas_proyecto {
id bigint [pk, increment]

proyecto_id bigint [not null, ref: > proyectos.id]

fecha_snapshot date

costo_planificado decimal(14,2)
costo_real decimal(14,2)

ingreso_planificado decimal(14,2)
ingreso_real decimal(14,2)

margen_planificado decimal(14,2)
margen_real decimal(14,2)

horas_planificadas decimal(10,2)
horas_reales decimal(10,2)

created_at timestamp
}

// ======================================================
// AUDITORÍA
// ======================================================

Table auditoria {
id bigint [pk, increment]

tipo_usuario varchar(50)
usuario_id bigint

entidad varchar(120)
entidad_id bigint

accion varchar(50)
// CREAR
// ACTUALIZAR
// ELIMINAR

valores_anteriores text
valores_nuevos text

created_at timestamp
}
