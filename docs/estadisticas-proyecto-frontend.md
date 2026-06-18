# Estadisticas de proyecto para frontend

Este documento reemplaza la lectura centrada en CPI/SPI para la pantalla principal del proyecto. La vista debe mostrar estado, horas invertidas contra horas planificadas, costo total del proyecto y costo por empleado segun las horas aprobadas y el costo hora aplicado desde su sueldo/tarifa del proyecto.

## Endpoint recomendado

```txt
GET /api/proyectos/{proyectoId}/dashboard-owner
```

Request: no tiene body. Usa el JWT en cookie HTTP-only.

El frontend debe consumir principalmente:

- `estadisticas`
- `costosPorEmpleado`
- `resumenHoras`
- `alertas`
- `semaforo`

`rentabilidad.cpi` y `rentabilidad.spi` se mantienen por compatibilidad, pero ya no son la base de la pantalla ni del semaforo.

## Response ejemplo

```json
{
  "proyecto": {
    "id": 4,
    "nombre": "Implementacion ProfitTrack",
    "estado": "EN_PROCESO",
    "horasPlanificadas": 160.00,
    "horasReales": 35.50,
    "presupuestoPlanificado": 12000.00,
    "costoReal": 2500.00,
    "precioVenta": 15000.00
  },
  "estadisticas": {
    "proyectoId": 4,
    "proyectoNombre": "Implementacion ProfitTrack",
    "estado": "EN_PROCESO",
    "semaforo": "AMARILLO",
    "horasPlanificadas": 160.00,
    "horasInvertidas": 35.50,
    "horasPendientes": 4.50,
    "horasDesaprobadas": 0.00,
    "avanceHorasPorcentaje": 22.1875,
    "horasExcedidas": 0.00,
    "costoLaboral": 2000.00,
    "costoOperativo": 500.00,
    "costoTotalProyecto": 2500.00,
    "costoPlanificado": 12000.00,
    "saldoPresupuesto": 9500.00,
    "porcentajePresupuestoConsumido": 20.8333,
    "costoPromedioHoraProyecto": 56.3380
  },
  "costosPorEmpleado": [
    {
      "empleadoId": 7,
      "empleadoNombre": "Backend Developer",
      "totalHoras": 25.00,
      "costoHoraPromedio": 50.00,
      "ultimoCostoHoraAplicado": 50.00,
      "totalCosto": 1250.00,
      "porcentajeCostoLaboral": 62.5000,
      "registros": 5
    }
  ],
  "resumenHoras": {
    "totalHorasRegistradas": 40.00,
    "totalHorasAprobadas": 35.50,
    "totalHorasPendientes": 4.50,
    "totalHorasRechazadas": 0.00
  },
  "semaforo": "AMARILLO",
  "alertas": [
    "Hay horas pendientes de aprobacion"
  ]
}
```

## Reglas de negocio

### Horas

```txt
horasPlanificadas = proyecto.horasPlanificadas
horasInvertidas = suma de registro_horas.horas_trabajadas donde estadoAprobacion = APROBADO y activo = true
horasPendientes = suma de registro_horas.horas_trabajadas donde estadoAprobacion = PENDIENTE y activo = true
horasDesaprobadas = suma de registro_horas.horas_trabajadas donde estadoAprobacion = DESAPROBADO y activo = true
avanceHorasPorcentaje = horasInvertidas / horasPlanificadas * 100
horasExcedidas = max(horasInvertidas - horasPlanificadas, 0)
```

Solo las horas aprobadas son horas oficiales del proyecto. Las pendientes sirven para alerta, pero no suman al costo ni al avance oficial.

### Costo del proyecto

```txt
costoLaboral = suma de costos_registro_horas.costo_total para registros aprobados y activos
costoOperativo = suma de egresos activos del proyecto
costoTotalProyecto = costoLaboral + costoOperativo
costoPlanificado = proyecto.presupuestoPlanificado
saldoPresupuesto = costoPlanificado - costoTotalProyecto
porcentajePresupuestoConsumido = costoTotalProyecto / costoPlanificado * 100
costoPromedioHoraProyecto = costoLaboral / horasInvertidas
```

Si no hay horas aprobadas o no hay planificacion, los porcentajes y promedios devuelven `0`.

### Costo por empleado

Cada fila de `costosPorEmpleado` agrupa solo registros aprobados y activos del proyecto.

```txt
totalHoras = suma de horas aprobadas del empleado en el proyecto
totalCosto = suma de costo_total del empleado en el proyecto
costoHoraPromedio = totalCosto / totalHoras
ultimoCostoHoraAplicado = ultimo costo_hora usado al calcular un registro aprobado
porcentajeCostoLaboral = totalCosto / costoLaboral * 100
registros = cantidad de registros aprobados usados en el calculo
```

El costo hora viene del costo aplicado al empleado dentro del proyecto. Ese costo se origina desde su sueldo/base de planilla o desde una actualizacion manual de tarifa, y queda guardado al aprobar horas para que el historico no cambie si luego cambia el sueldo.

## Semaforo actual

El semaforo ya no depende de CPI/SPI.

### Rojo

```txt
estado = CANCELADO
o margenReal < 0
o costoTotalProyecto > costoPlanificado, si hay presupuesto planificado
o horasInvertidas > horasPlanificadas, si hay horas planificadas
o estado = FINALIZADO y esRentable = false
```

### Amarillo

```txt
hay horas pendientes de aprobacion
o costoTotalProyecto consumio al menos 80% del presupuesto planificado
o horasInvertidas consumio al menos 80% de horas planificadas
o hay costos reales sin presupuesto planificado
o hay horas invertidas sin horas planificadas
o hay costos reales y aun no hay ingresos registrados
```

### Verde

```txt
No cae en rojo ni amarillo.
```

## Que debe pintar frontend

Cards principales:

- Estado del proyecto: `estadisticas.estado`
- Semaforo: `estadisticas.semaforo`
- Horas: `horasInvertidas` vs `horasPlanificadas`
- Avance de horas: `avanceHorasPorcentaje`
- Costo total: `costoTotalProyecto`
- Costo laboral: `costoLaboral`
- Costo operativo: `costoOperativo`
- Presupuesto restante: `saldoPresupuesto`

Tabla de empleados:

- `empleadoNombre`
- `totalHoras`
- `costoHoraPromedio`
- `ultimoCostoHoraAplicado`
- `totalCosto`
- `porcentajeCostoLaboral`
- `registros`

Alertas:

- Pintar todos los textos de `alertas`.
- Si `horasPendientes > 0`, mostrar una accion para que lider/owner revise aprobaciones.

