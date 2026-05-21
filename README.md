# Paso a paso
## OWner
Tiene acceso a estos endpoints:
- /empleados
- /clientes
- /tipo-servicio
- /tipo-tarea
- /historial-costo-hora
    - para definir el precio actual e historico del empleado
- /proyectos
- /proyecto-empleado
    - asignar un grupo de personal al proyecto
- /tareas
    - crear tareas asignadas a un empleado


- ## Empleado
    Dbveria hacer estos pasos:
    - /tareas/proyeto/1
        - ve sus taraes asignadas y las hace, las finaliza
    - /registro-horas
        - asigna sus horas en su timesheet diario, esto no se sobrescribe la tarea, si no es un registro de sus actividades diarias
- ### OWNER APRUEBA
    - /registro-horas/{id}/aprobar
        - esto toma el coto mas reciente modificado con /historia-costo-hora pero regidstrado en la tabla proyecto_costo_empleado
    
    - /metricas/proyecto/1/actual
        - para ver la rentabilidad actual