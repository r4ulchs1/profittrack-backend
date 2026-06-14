# Flujo Owner: metricas y estado del proyecto

Este documento se enfoca solo en el flujo para que el Owner vea como va un proyecto: horas, costos, ingresos, margen, rentabilidad y alertas.

Base URL local:

```txt
http://localhost:8080
```

Todas las APIs usan la sesion JWT en cookies HTTP-only.

## Objetivo de la pantalla

El Owner deberia poder responder rapidamente:

- Cuanto se vendio o se planea vender el proyecto.
- Cuanto se planifico gastar.
- Cuanto se esta gastando realmente.
- Cuantas horas se planificaron vs cuantas se aprobaron.
- Si el proyecto esta rentable o perdiendo dinero.
- Que empleados estan generando mayor costo.
- Si hay horas pendientes de aprobacion.
- Si el proyecto va bien, en riesgo o mal.

## Flujo recomendado de carga

Para una pantalla de detalle financiero del proyecto:

1. Usar la API consolidada `GET /api/proyectos/{proyectoId}/dashboard-owner`.
2. Opcional: generar snapshot para guardar historico.
3. Opcional: listar historial de costo hora aplicado al proyecto si quieres una vista dedicada.

La pantalla tambien se puede armar con APIs separadas, pero la recomendada para frontend es la consolidada.

Para una pantalla financiera general de empresa:

```txt
GET /api/empresas/dashboard-financiero-owner
```

## Dashboard financiero de empresa

### GET `/api/empresas/dashboard-financiero-owner`

Funcion: devuelve un consolidado financiero de todos los proyectos activos de la empresa, mas ingresos y egresos reales de la empresa.

Request: no tiene body. Usa la empresa del usuario autenticado.

Response:

```json
{
  "empresaId": 1,
  "empresaNombre": "ProfitTrack",
  "fechaConsulta": "2026-06-03",
  "totalIngresoPlanificado": 45000.00,
  "totalIngresoReal": 12000.00,
  "totalCostoPlanificado": 30000.00,
  "totalCostoLaboral": 8000.00,
  "totalEgresoReal": 2500.00,
  "totalCostoReal": 10500.00,
  "margenPlanificado": 15000.00,
  "margenReal": 1500.00,
  "porcentajeMargen": 12.50,
  "horasPlanificadas": 520.00,
  "horasReales": 180.00,
  "cpi": 2.8571,
  "spi": 0.3462,
  "totalProyectos": 3,
  "proyectosRentables": 2,
  "proyectosEnRiesgo": 1,
  "semaforo": "AMARILLO",
  "alertas": [
    "Hay 1 proyecto(s) en riesgo"
  ],
  "proyectos": [
    {
      "proyectoId": 4,
      "proyectoNombre": "Implementacion ProfitTrack",
      "estado": "EN_PROCESO",
      "semaforo": "VERDE",
      "ingresoPlanificado": 15000.00,
      "ingresoReal": 3500.00,
      "costoPlanificado": 12000.00,
      "costoLaboral": 2000.00,
      "costoOpex": 500.00,
      "costoReal": 2500.00,
      "margenPlanificado": 3000.00,
      "margenReal": 1000.00,
      "porcentajeMargen": 28.57,
      "horasPlanificadas": 160.00,
      "horasReales": 35.50,
      "cpi": 4.8000,
      "spi": 0.2219,
      "esRentable": true
    }
  ],
  "ingresos": [],
  "egresos": []
}
```

Que pintar:

- Total ingresos reales vs planificados.
- Total costos reales vs planificados.
- Costo laboral y egresos reales.
- Margen real y margen planificado.
- CPI/SPI global.
- Proyectos rentables y proyectos en riesgo.
- Semaforo global.
- Alertas globales.
- Tabla de proyectos con su propio semaforo.

## 0. Dashboard Owner consolidado

### GET `/api/proyectos/{proyectoId}/dashboard-owner`

Funcion: devuelve en una sola respuesta todo lo necesario para la vista Owner del proyecto.

Response:

```json
{
  "proyecto": {},
  "rentabilidad": {},
  "resumenHoras": {},
  "costosPorEmpleado": [
    {
      "empleadoId": 7,
      "empleadoNombre": "Backend Developer",
      "totalHoras": 25.00,
      "totalCosto": 1250.00,
      "registros": 5
    }
  ],
  "equipo": [],
  "costosAplicados": [],
  "ingresos": [],
  "egresos": [],
  "snapshots": [],
  "semaforo": "VERDE",
  "alertas": [
    "Hay horas pendientes de aprobacion"
  ]
}
```

Campos:

- `proyecto`: datos base y planificados del proyecto.
- `rentabilidad`: calculo vivo de costos, ingresos, margen, CPI, SPI.
- `resumenHoras`: horas registradas, aprobadas y pendientes.
- `costosPorEmpleado`: costo laboral generado por empleado.
- `equipo`: empleados asignados y costo hora aplicado.
- `costosAplicados`: historial de costo hora aplicado por empleado dentro del proyecto.
- `ingresos`: ingresos reales del proyecto.
- `egresos`: costos operativos del proyecto.
- `snapshots`: historico de metricas guardadas.
- `semaforo`: `VERDE`, `AMARILLO` o `ROJO`.
- `alertas`: motivos concretos para mostrar avisos al Owner.

## 1. Datos base del proyecto

### GET `/api/proyectos/{proyectoId}`

Funcion: trae informacion planificada y estado general del proyecto.

Usalo para pintar:

- Nombre del proyecto.
- Estado: `PLANIFICADO`, `EN_PROCESO`, `PAUSADO`, `FINALIZADO`, `CANCELADO`.
- Precio venta planificado.
- Presupuesto planificado.
- Margen planificado.
- Horas planificadas.
- Horas reales guardadas.
- Etapas.
- Fechas planificadas y reales.

Response ejemplo:

```json
{
  "id": 4,
  "nombre": "Implementacion ProfitTrack",
  "estado": "EN_PROCESO",
  "horasPlanificadas": 160.00,
  "horasReales": 35.50,
  "presupuestoPlanificado": 12000.00,
  "costoReal": 2500.00,
  "margenPlanificado": 3000.00,
  "margenReal": 1000.00,
  "precioVenta": 15000.00,
  "fechaInicioPlanificada": "2026-06-01",
  "fechaFinPlanificada": "2026-08-31",
  "fechaInicioReal": "2026-06-02",
  "fechaFinReal": null,
  "etapas": [
    {
      "id": 10,
      "nombre": "Backend",
      "horasPlanificadas": 80.00,
      "horasTareasPlanificadas": 60.00,
      "horasReales": 20.00,
      "estado": "EN_CURSO"
    }
  ]
}
```

Notas:

- `precioVenta` representa lo que se espera cobrar/vender.
- `presupuestoPlanificado` representa el costo planificado.
- `margenPlanificado` deberia ser `precioVenta - presupuestoPlanificado`.
- `costoReal` y `margenReal` se actualizan al generar metricas/snapshot.

## 2. Rentabilidad actual

### GET `/api/metricas/proyecto/{proyectoId}/actual`

Funcion: calcula en vivo como va financieramente el proyecto.

Esta es la API principal para el dashboard Owner.

Response:

```json
{
  "proyectoId": 4,
  "proyectoNombre": "Implementacion ProfitTrack",
  "estado": "EN_PROCESO",
  "costoLaboral": 2000.00,
  "costoOpex": 500.00,
  "costoReal": 2500.00,
  "costoPlanificado": 12000.00,
  "ingresoReal": 3500.00,
  "ingresoPlanificado": 15000.00,
  "margenReal": 1000.00,
  "margenPlanificado": 3000.00,
  "porcentajeMargen": 28.57,
  "horasReales": 35.50,
  "horasPlanificadas": 160.00,
  "cpi": 4.80,
  "spi": 0.22,
  "esRentable": true
}
```

Como se calcula:

```txt
costoLaboral = suma de costos de horas aprobadas
costoOpex = suma de egresos del proyecto
costoReal = costoLaboral + costoOpex
ingresoReal = suma de ingresos registrados del proyecto
ingresoPlanificado = proyecto.precioVenta
costoPlanificado = proyecto.presupuestoPlanificado
margenReal = ingresoReal - costoReal
margenPlanificado = ingresoPlanificado - costoPlanificado
porcentajeMargen = margenReal / ingresoReal * 100
cpi = costoPlanificado / costoReal
spi = horasReales / horasPlanificadas
esRentable = margenReal > 0
```

Que pintar:

- Card: Ingreso planificado.
- Card: Ingreso real.
- Card: Costo planificado.
- Card: Costo real.
- Card: Margen planificado.
- Card: Margen real.
- Card: Porcentaje margen.
- Indicador: `esRentable`.
- Indicador: `cpi`.
- Indicador: `spi`.

## 3. Resumen de horas

### GET `/api/registro-horas/resumen?proyectoId={proyectoId}`

Funcion: resume horas registradas, aprobadas y pendientes.

Response:

```json
{
  "totalHorasRegistradas": 40.00,
  "totalHorasAprobadas": 35.50,
  "totalHorasPendientes": 4.50,
  "totalHorasRechazadas": 0.00,
  "horasPorProyecto": [
    {
      "proyectoId": 4,
      "proyectoNombre": "Implementacion ProfitTrack",
      "horas": 40.00
    }
  ],
  "horasPorEmpleado": [
    {
      "empleadoId": 7,
      "empleadoNombre": "Backend Developer",
      "horas": 25.00
    }
  ]
}
```

Query params opcionales:

```txt
proyectoId
empleadoId
fechaInicio
fechaFin
```

Que pintar:

- Horas registradas.
- Horas aprobadas.
- Horas pendientes.
- Horas por empleado.
- Alerta si `totalHorasPendientes > 0`.

Nota: `totalHorasRechazadas` hoy devuelve `0` porque registro de horas aun no diferencia pendiente vs rechazado con enum.

## 4. Costo laboral por empleado

### GET `/api/costos-registro/proyecto/{proyectoId}/resumen`

Funcion: agrupa el costo de horas aprobadas por empleado.

Response:

```json
[
  {
    "empleadoId": 7,
    "empleadoNombre": "Backend Developer",
    "totalHoras": 25.00,
    "totalCosto": 1250.00,
    "registros": 5
  }
]
```

Que pintar:

- Tabla de empleados.
- Total de horas aprobadas por empleado.
- Total de costo generado por empleado.
- Cantidad de registros aprobados.

Uso Owner:

- Detectar que empleado esta consumiendo mas presupuesto.
- Comparar costo laboral vs avance real.
- Ver si un proyecto esta perdiendo margen por horas.

## 5. Equipo y costo hora aplicado

### GET `/api/proyecto-empleados/proyecto/{proyectoId}`

Funcion: lista empleados asignados al proyecto y el costo hora congelado/aplicado al proyecto.

Response:

```json
[
  {
    "id": 20,
    "proyectoId": 4,
    "proyectoNombre": "Implementacion ProfitTrack",
    "empleadoId": 7,
    "empleadoNombre": "Backend Developer",
    "rolAsignado": "LIDER",
    "fechaAsignacion": "2026-06-03T15:00:00Z",
    "costoHoraCongelado": 50.00,
    "activo": true
  }
]
```

Que pintar:

- Empleado.
- Rol asignado.
- Costo hora aplicado al proyecto.
- Fecha de asignacion.

Relacion con costos:

- `costoHoraCongelado` es la tarifa usada para calcular costo laboral al aprobar horas.
- Luego cada hora aprobada genera registro en `costos_registro_horas`.

## 6. Ingresos reales

### GET `/api/ingresos/proyecto/{proyectoId}`

Funcion: lista ingresos registrados del proyecto.

Response:

```json
[
  {
    "id": 1,
    "empresaId": 1,
    "proyectoId": 4,
    "proyectoNombre": "Implementacion ProfitTrack",
    "tipo": "FACTURA",
    "monto": 3500.00,
    "fechaIngreso": "2026-06-03",
    "descripcion": "Primer pago",
    "activo": true
  }
]
```

Que pintar:

- Lista de pagos/facturas.
- Total de ingresos reales.
- Comparacion contra `ingresoPlanificado`.

## 7. Egresos reales

### GET `/api/egresos/proyecto/{proyectoId}`

Funcion: lista costos operativos del proyecto.

Response:

```json
[
  {
    "id": 1,
    "empresaId": 1,
    "proyectoId": 4,
    "proyectoNombre": "Implementacion ProfitTrack",
    "categoriaId": 1,
    "categoriaNombre": "Infraestructura",
    "monto": 500.00,
    "fechaEgreso": "2026-06-03",
    "descripcion": "Servidor cloud",
    "activo": true
  }
]
```

Que pintar:

- Lista de egresos.
- Total costo operativo.
- Comparacion contra costo laboral.

## 8. Guardar snapshot historico

### POST `/api/metricas/proyecto/{proyectoId}/snapshot`

Funcion: calcula rentabilidad y guarda una foto historica.

Usarlo cuando:

- El Owner presiona "Guardar snapshot".
- Se quiere registrar cierre semanal/mensual.
- Se quiere graficar evolucion historica.

Response:

```json
{
  "id": 1,
  "proyectoId": 4,
  "fechaSnapshot": "2026-06-03",
  "costoPlanificado": 12000.00,
  "costoReal": 2500.00,
  "costoLaboral": 2000.00,
  "costoOpex": 500.00,
  "ingresoPlanificado": 15000.00,
  "ingresoReal": 3500.00,
  "margenPlanificado": 3000.00,
  "margenReal": 1000.00,
  "horasPlanificadas": 160.00,
  "horasReales": 35.50
}
```

Importante:

- Esta API tambien actualiza `costoReal`, `horasReales`, `margenReal` y `margenPlanificado` del proyecto.

## 9. Historial de snapshots

### GET `/api/metricas/proyecto/{proyectoId}`

Funcion: listar snapshots guardados para graficar evolucion.

Response:

```json
[
  {
    "id": 1,
    "proyectoId": 4,
    "fechaSnapshot": "2026-06-03",
    "costoPlanificado": 12000.00,
    "costoReal": 2500.00,
    "costoLaboral": null,
    "costoOpex": null,
    "ingresoPlanificado": 15000.00,
    "ingresoReal": 3500.00,
    "margenPlanificado": 3000.00,
    "margenReal": 1000.00,
    "horasPlanificadas": 160.00,
    "horasReales": 35.50
  }
]
```

Que graficar:

- `fechaSnapshot` vs `costoReal`.
- `fechaSnapshot` vs `margenReal`.
- `fechaSnapshot` vs `horasReales`.
- Comparar `costoReal` contra `costoPlanificado`.

## Semaforo para frontend

Con `RentabilidadResponse`:

### Verde

```txt
esRentable = true
cpi >= 1
spi <= 1
margenReal >= 0
```

Mensaje sugerido:

```txt
Proyecto rentable y dentro del presupuesto.
```

### Amarillo

```txt
esRentable = true
cpi >= 0.8 y cpi < 1
o spi > 1 y spi <= 1.2
o totalHorasPendientes > 0
```

Mensaje sugerido:

```txt
Proyecto rentable, pero con riesgo en costos, horas o aprobaciones pendientes.
```

### Rojo

```txt
esRentable = false
o margenReal < 0
o cpi < 0.8
o spi > 1.2
```

Mensaje sugerido:

```txt
Proyecto en riesgo: revisar costos, horas e ingresos.
```

## Cards recomendadas

Primera fila:

- Estado del proyecto.
- Precio venta / ingreso planificado.
- Ingreso real.
- Margen real.
- Semaforo.

Segunda fila:

- Costo planificado.
- Costo real.
- Costo laboral.
- Costo operativo.

Tercera fila:

- Horas planificadas.
- Horas reales/aprobadas.
- Horas pendientes.
- Avance de horas: `horasReales / horasPlanificadas`.

Tablas:

- Costo por empleado.
- Horas por empleado.
- Ingresos reales.
- Egresos reales.
- Etapas y tareas con avance.

## Alertas recomendadas

Crear alertas visuales si:

- `margenReal < 0`: el proyecto esta perdiendo dinero.
- `costoReal > costoPlanificado`: se supero el presupuesto.
- `horasReales > horasPlanificadas`: se superaron horas planificadas.
- `totalHorasPendientes > 0`: hay horas por aprobar.
- `ingresoReal = 0` y hay costoReal: hay costos sin ingresos registrados.
- `costoLaboral > costoOpex * 2`: el costo principal viene de horas.
- `estado = FINALIZADO` y `esRentable = false`: proyecto cerrado con perdida.

## Orden exacto de llamadas para la pantalla

Opcion recomendada:

```txt
GET /api/proyectos/{proyectoId}/dashboard-owner
```

Opcion desagregada:

```txt
GET /api/proyectos/{proyectoId}
GET /api/metricas/proyecto/{proyectoId}/actual
GET /api/registro-horas/resumen?proyectoId={proyectoId}
GET /api/costos-registro/proyecto/{proyectoId}/resumen
GET /api/proyecto-empleados/proyecto/{proyectoId}
GET /api/ingresos/proyecto/{proyectoId}
GET /api/egresos/proyecto/{proyectoId}
GET /api/metricas/proyecto/{proyectoId}
```

Para refrescar metricas historicas:

```txt
POST /api/metricas/proyecto/{proyectoId}/snapshot
GET /api/metricas/proyecto/{proyectoId}
GET /api/metricas/proyecto/{proyectoId}/actual
```

## Historial de costo aplicado al proyecto

### GET `/api/proyecto-costo-empleado/proyecto/{proyectoId}`

Funcion: lista el historial de costos hora aplicados a empleados dentro del proyecto.

Esto sirve para ver que tarifa uso el proyecto por empleado y desde cuando.

Response:

```json
[
  {
    "id": 1,
    "proyectoId": 4,
    "proyectoNombre": "Implementacion ProfitTrack",
    "empleadoId": 7,
    "empleadoNombre": "Backend Developer",
    "costoHora": 50.00,
    "fechaInicio": "2026-06-01",
    "fechaFin": null,
    "vigente": true,
    "activo": true
  }
]
```

Notas:

- Si `fechaFin` es `null`, esa tarifa esta vigente.
- `costoHora` es la tarifa aplicada al proyecto, no necesariamente la tarifa global actual del empleado.
- El dashboard consolidado ya incluye esta lista en `costosAplicados`.
