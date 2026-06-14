# Nueva logica de tareas, horas y costos por proyecto

Este documento define el cambio de perspectiva del flujo de proyectos. La unidad principal ya no es una tarea planificada por fechas y luego un registro diario separado, sino una accion unica donde un miembro del proyecto declara la tarea que realizo y el total de horas dedicadas. Despues, el lider del proyecto aprueba o desaprueba esa declaracion.

## Resumen del cambio

Antes:

- El lider, PM, gerente u owner creaba tareas del proyecto.
- El empleado seleccionaba una tarea existente y registraba horas aparte.
- El sistema manejaba fechas planificadas, fechas reales, fecha de trabajo, hora de ingreso y hora de salida.
- Los costos se calculaban cuando se aprobaba un registro de horas.

Ahora:

- Cualquier empleado asignado al proyecto puede crear la tarea que realizo.
- La tarea y el total de horas dedicadas se registran en una sola accion.
- El usuario no ingresa fechas manuales.
- El sistema usa la hora del sistema para trazabilidad: creacion, actualizacion, aprobacion, rechazo y calculo de costo.
- El lider del proyecto aprueba o desaprueba la tarea con horas.
- La tarea creada solo puede ser editada por el usuario que la creo.
- Solo las tareas/horas aprobadas suman al costo final, horas reales y graficos oficiales del proyecto.

## Regla central de negocio

Cada registro representa una declaracion de trabajo hecha por un empleado:

```txt
Empleado del proyecto crea tarea realizada + total de horas dedicadas
```

El flujo completo es:

1. El empleado autenticado pertenece al proyecto.
2. El empleado registra en una sola accion:
   - proyecto
   - etapa, si aplica
   - tipo de tarea, si aplica
   - nombre de la tarea realizada
   - descripcion, si aplica
   - total de horas dedicadas
3. El sistema crea la tarea y el registro de horas asociado.
4. El registro queda en estado `PENDIENTE`.
5. El lider revisa.
6. Si aprueba, las horas y el costo pasan a metricas oficiales.
7. Si desaprueba, no suma horas reales ni costo.

## Fechas y hora del sistema

Se eliminan las fechas manuales del flujo de tareas y horas:

- No se pide `fechaTrabajo`.
- No se pide `horaIngreso`.
- No se pide `horaSalida`.
- No se pide `fechaInicioPlanificada` ni `fechaFinPlanificada` para tareas.
- No se pide `fechaInicioReal` ni `fechaFinReal` para tareas.

El backend debe usar timestamps del sistema:

- `creadoEn`: momento en que el empleado registra la tarea con horas.
- `actualizadoEn`: ultima modificacion de la tarea/registro.
- `aprobadoEn`: momento en que el lider aprueba.
- `rechazadoEn`: momento en que el lider desaprueba.
- `fechaCalculo`: momento en que se calcula el costo aprobado.

La planificacion del proyecto se expresa en horas, no en fechas. Por ejemplo, un proyecto puede tener `44` horas planificadas.

## Permisos

### Empleado asignado al proyecto

Puede:

- Crear una tarea realizada con sus horas.
- Ver sus propias tareas/horas.
- Editar solo las tareas/horas que creo.

No puede:

- Crear tareas para otro empleado.
- Editar tareas creadas por otro empleado.
- Aprobar o desaprobar tareas, salvo que tambien sea lider autorizado.
- Cambiar el costo hora aplicado.

### Lider del proyecto

Puede:

- Ver todas las tareas/horas del proyecto.
- Aprobar tareas/horas pendientes.
- Desaprobar tareas/horas pendientes.
- Ver costos, metricas y graficos del proyecto.

No deberia editar el contenido de la tarea creada por el empleado. Su accion es aprobar o desaprobar.

### PM, gerente u owner

Pueden mantener permisos administrativos existentes, pero para este flujo deben respetar la misma regla de auditoria:

- La autoria de la tarea pertenece al empleado que la creo.
- La aprobacion pertenece al lider o rol autorizado.
- La edicion del contenido pertenece al creador.

## Estados recomendados

El booleano actual `aprobado` no alcanza para diferenciar pendiente, aprobado y desaprobado. Se recomienda reemplazarlo o complementarlo con un estado explicito:

```txt
PENDIENTE
APROBADO
DESAPROBADO
```

Reglas:

- Al crear: `PENDIENTE`.
- Al aprobar: `APROBADO`.
- Al desaprobar: `DESAPROBADO`.
- Solo `APROBADO` suma a costos y horas reales.
- `PENDIENTE` aparece como trabajo pendiente de revision.
- `DESAPROBADO` queda como historial/auditoria, pero no afecta costo ni avance.

## Endpoint unificado propuesto

Para evitar que frontend haga dos llamadas separadas, el flujo deberia tener un endpoint unico.

```txt
POST /api/tareas/realizadas
```

Request:

```json
{
  "proyectoId": 1,
  "etapaProyectoId": 3,
  "tipoTareaId": 2,
  "nombre": "Implementar pantalla de login",
  "descripcion": "Se completo la integracion del formulario con autenticacion",
  "horasDedicadas": 20
}
```

Notas:

- `empleadoId` no debe venir en el body.
- El empleado se obtiene del usuario autenticado.
- `horasDedicadas` es obligatorio y debe ser mayor que `0`.
- No se envian fechas.
- No se envian hora de ingreso ni hora de salida.

Response sugerido:

```json
{
  "tareaId": 10,
  "registroHorasId": 50,
  "proyectoId": 1,
  "empleadoId": 7,
  "empleadoNombre": "Empleado Uno",
  "nombre": "Implementar pantalla de login",
  "descripcion": "Se completo la integracion del formulario con autenticacion",
  "horasDedicadas": 20,
  "estadoAprobacion": "PENDIENTE",
  "creadoEn": "2026-06-11T15:30:00Z"
}
```

## Aprobacion y desaprobacion

Se pueden adaptar los endpoints actuales de registro de horas o crear rutas orientadas al nuevo concepto.

Opcion recomendada:

```txt
PATCH /api/tareas-realizadas/{registroHorasId}/aprobar
PATCH /api/tareas-realizadas/{registroHorasId}/desaprobar
```

Tambien se puede reutilizar:

```txt
PATCH /api/registro-horas/{id}/aprobar
PATCH /api/registro-horas/{id}/rechazar
```

Reglas al aprobar:

1. Validar que quien aprueba sea lider del proyecto o rol autorizado.
2. Validar que el registro este pendiente.
3. Validar que el empleado tenga costo hora aplicado en el proyecto.
4. Calcular:

```txt
costoTotal = horasDedicadas * costoHoraEmpleadoEnProyecto
```

5. Crear o actualizar `costo_registro_horas`.
6. Marcar estado como `APROBADO`.
7. Guardar `aprobadoEn` con hora del sistema.
8. Incluir esas horas en metricas y graficos.

Reglas al desaprobar:

1. Validar que quien desaprueba sea lider del proyecto o rol autorizado.
2. Marcar estado como `DESAPROBADO`.
3. Guardar `rechazadoEn` con hora del sistema.
4. Eliminar o invalidar cualquier costo calculado previo.
5. No sumar esas horas a costo final ni horas reales.

## Edicion de tarea creada

La tarea con horas solo puede ser editada por el usuario que la creo.

Reglas recomendadas:

- Si esta `PENDIENTE`, el creador puede editar nombre, descripcion, tipo, etapa y horas.
- Si esta `DESAPROBADO`, el creador puede corregirla y reenviarla a `PENDIENTE`.
- Si esta `APROBADO`, queda bloqueada para proteger los costos ya calculados.
- Si se decide permitir edicion de una aprobada, entonces la edicion debe:
  - volver el estado a `PENDIENTE`
  - eliminar el costo aprobado anterior
  - recalcular metricas cuando el lider apruebe de nuevo

## Impacto en modelos actuales

### `TareaProyecto`

Campos que dejan de usarse en este flujo:

- `fechaInicioPlanificada`
- `fechaFinPlanificada`
- `fechaInicioReal`
- `fechaFinReal`

Campos/reglas a agregar o reforzar:

- `empleadoCreador` o usar `empleadoAsignado` como creador obligatorio.
- `horasReales` puede reflejar las horas aprobadas de esa tarea.
- `estado` puede quedar como estado operativo de tarea, pero no debe reemplazar el estado de aprobacion.

### `RegistroHoras`

Campos que dejan de usarse en este flujo:

- `fechaTrabajo`
- `horaIngreso`
- `horaSalida`
- `minutosDescanso`

Campos/reglas a agregar o reforzar:

- `horasTrabajadas` representa el total de horas dedicadas.
- `estadoAprobacion`: `PENDIENTE`, `APROBADO`, `DESAPROBADO`.
- `aprobadoEn`, `rechazadoEn` para trazabilidad con hora del sistema.
- `empleado` siempre debe ser el usuario autenticado que creo la tarea.

### `CostoRegistroHoras`

Se mantiene, pero solo debe existir para registros aprobados.

Regla:

```txt
costoTotal = registroHoras.horasTrabajadas * proyectoCostoEmpleado.costoHora
```

`fechaCalculo` se toma con la hora del sistema.

### `Proyecto`

Se mantiene el concepto de horas planificadas:

- `horasPlanificadas`: estimacion total del proyecto.
- `horasReales`: suma de horas aprobadas.
- `costoReal`: costo laboral aprobado + egresos operativos.
- `margenReal`: ingreso real - costo real.

Si el producto decide eliminar fechas tambien del proyecto, deben retirarse del request/response y ocultarse en frontend:

- `fechaInicioPlanificada`
- `fechaFinPlanificada`
- `fechaInicioReal`
- `fechaFinReal`

## Calculo de costo final del proyecto

El costo laboral del proyecto es la suma de las horas aprobadas por cada empleado multiplicadas por su costo hora aplicado al proyecto.

Formula:

```txt
costoLaboralProyecto = sumatoria(horasAprobadasEmpleado * costoHoraEmpleadoProyecto)
costoFinalProyecto = costoLaboralProyecto + egresosOperativosProyecto
```

Si no hay egresos operativos:

```txt
costoFinalProyecto = costoLaboralProyecto
```

Ejemplo:

```txt
Proyecto planificado: 44 horas

Empleado A:
horas aprobadas = 20
costo hora = 60
costo = 20 * 60 = 1200

Empleado B:
horas aprobadas = 24
costo hora = 55
costo = 24 * 55 = 1320

costo laboral final = 1200 + 1320 = 2520
horas reales aprobadas = 20 + 24 = 44
```

Resultado:

```json
{
  "horasPlanificadas": 44,
  "horasReales": 44,
  "costoLaboral": 2520,
  "costoOpex": 0,
  "costoFinalProyecto": 2520
}
```

## Graficos y dashboard

Los graficos deben usar solo horas aprobadas para metricas oficiales.

### Horas planificadas vs horas reales

```txt
horasPlanificadas = proyecto.horasPlanificadas
horasReales = suma de horas de registros APROBADOS
```

Con el ejemplo:

```txt
44 planificadas vs 44 reales
```

### Costo por empleado

```txt
Empleado A = 1200
Empleado B = 1320
```

Esto permite mostrar una barra o tabla:

```txt
Empleado | Horas | Costo hora | Costo total
A        | 20    | 60         | 1200
B        | 24    | 55         | 1320
Total    | 44    | -          | 2520
```

### Costo final del proyecto

```txt
costoFinalProyecto = 2520 + egresosOperativos
```

### Avance de horas

```txt
avanceHoras = horasReales / horasPlanificadas
avanceHoras = 44 / 44 = 1.00 = 100%
```

### CPI y SPI

Si se mantiene la logica actual:

```txt
cpi = costoPlanificado / costoReal
spi = horasReales / horasPlanificadas
```

Interpretacion:

- `spi = 1`: se consumieron exactamente las horas estimadas.
- `spi > 1`: se consumieron mas horas de las estimadas.
- `spi < 1`: aun no se consumen todas las horas estimadas.
- `cpi >= 1`: el costo real esta dentro del presupuesto.
- `cpi < 1`: el costo real supero el presupuesto.

## Cambios necesarios en backend

1. Crear DTO para la accion unica de tarea realizada con horas.
2. Crear use case/servicio transaccional que cree `TareaProyecto` y `RegistroHoras` juntos.
3. Eliminar fechas manuales de request/response del nuevo flujo.
4. Tomar empleado desde el contexto de seguridad, no desde el body.
5. Validar que el empleado pertenezca al proyecto.
6. Guardar estado inicial `PENDIENTE`.
7. Agregar estado explicito para aprobacion.
8. Restringir edicion al creador.
9. Restringir aprobacion/desaprobacion al lider o rol autorizado.
10. Calcular costo solo al aprobar.
11. No sumar pendientes ni desaprobadas a metricas oficiales.
12. Ajustar resumen de horas para separar `PENDIENTE`, `APROBADO` y `DESAPROBADO`.
13. Ajustar dashboard owner para mostrar tareas pendientes de revision.

## Cambios necesarios en frontend

1. Reemplazar el flujo de crear tarea y luego registrar horas por un solo formulario.
2. Quitar campos de fecha, hora ingreso, hora salida y descanso.
3. No permitir seleccionar empleado al crear: siempre es el usuario autenticado.
4. Mostrar estado de aprobacion de cada tarea.
5. Permitir editar solo tareas propias.
6. Bloquear edicion de tareas aprobadas, salvo que el backend defina reenvio a pendiente.
7. Agregar vista del lider para aprobar/desaprobar.
8. Mostrar graficos con horas/costos aprobados.
9. Mostrar pendientes como alerta, pero no sumarlas al costo oficial.

## Criterios de aceptacion

- Un empleado asignado a un proyecto puede crear una tarea realizada con horas en una sola accion.
- El body no acepta fechas ni horas de entrada/salida.
- El sistema registra timestamps automaticamente.
- La tarea queda pendiente hasta que el lider la revise.
- El lider puede aprobar o desaprobar.
- Una tarea desaprobada no suma al costo del proyecto.
- Una tarea pendiente no suma al costo del proyecto.
- Una tarea aprobada suma:
  - horas reales del proyecto
  - costo por empleado
  - costo laboral final
  - graficos oficiales
- Solo el creador puede editar su tarea.
- El costo final se calcula con la tarifa por hora aplicada al empleado dentro del proyecto.
