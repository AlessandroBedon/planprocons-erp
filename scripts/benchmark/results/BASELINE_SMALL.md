# Baseline SMALL — 10.006 registros

Medición realizada el 12 de agosto de 2026 contra el backend local de PlanProcons. Son resultados reales; no representan datos simulados.

## Entorno registrado

- Sistema operativo: Microsoft Windows NT 10.0.26200.0, x64.
- Procesador: Intel64 Family 6 Model 142 Stepping 10.
- Procesadores lógicos visibles: 4.
- Java del proyecto: 21.0.12.
- PostgreSQL: 17.10, x86-64 para Windows.
- PowerShell: 5.1.26100.6584.
- Base de datos: `planprocons_erp`, esquema `public`.
- Registros: 10.006.
- Rango almacenado: 2026-01-01 06:07:16 a 2026-08-12 09:10:00.
- Rango solicitado al benchmark: 2026-01-01 a 2026-08-12.
- Tamaño total de `registros_acceso`: aproximadamente 4.896 kB.
- Tabla: aproximadamente 1.544 kB.
- Índices: aproximadamente 3.320 kB.
- RAM total: no registrada; la consulta WMI fue denegada por permisos del entorno de ejecución.

## Metodología

- Una ejecución de calentamiento por endpoint, excluida de resultados.
- Cinco ejecuciones secuenciales medidas por endpoint.
- Latencia extremo a extremo local: HTTP, Spring Boot, PostgreSQL, serialización y transferencia.
- No se limpiaron cachés de PostgreSQL ni de Windows.
- Sin prueba de concurrencia.
- El resumen y accesos por hora usan el día `2026-08-12`, según su contrato real; los endpoints por rango usan todo el periodo indicado.

## Resultados

| Endpoint | Mínimo ms | Máximo ms | Promedio ms | Mediana ms |
|---|---:|---:|---:|---:|
| resumen | 30.648 | 223.207 | 85.718 | 42.730 |
| accesos_por_hora | 52.515 | 185.193 | 97.985 | 75.217 |
| accesos_por_dia | 33.727 | 490.787 | 261.809 | 307.556 |
| personas_frecuentes | 26.308 | 333.565 | 88.562 | 27.771 |
| patrones | 115.115 | 628.910 | 243.277 | 161.375 |
| anomalias_resumen | 15,328.977 | 19,858.290 | 17,661.116 | 17,776.668 |
| anomalias_pagina | 1,145.169 | 44,443.745 | 23,264.054 | 34,397.071 |

Todos los resultados tuvieron HTTP 200. El hash del campo `data` permaneció idéntico en las cinco repeticiones de cada endpoint, por lo que no se detectaron cambios funcionales durante esta ejecución.

## Índices encontrados antes de optimizar

- `registros_acceso_pkey`: único, `id`.
- `uk_registro_dispositivo_evento`: único, `dispositivo_id, codigo_evento`.
- `idx_registros_fecha_hora`: `fecha_hora`.
- `idx_registros_persona_fecha`: `persona_id, fecha_hora`.
- `idx_registros_dispositivo_fecha`: `dispositivo_id, fecha_hora`.
- `idx_registros_tipo_fecha`: `tipo_evento, fecha_hora`.
- `idx_registros_estado_fecha`: `estado, fecha_hora`.

No se creó, eliminó ni modificó ningún índice.

## Lectura inicial y riesgos

- `anomalias_resumen` es el cuello de botella más consistente del escenario SMALL.
- `anomalias_pagina` tiene alta dispersión. El endpoint ejecuta una CTE para obtener la página y vuelve a ejecutar la lógica para calcular el total, por lo que requiere análisis específico.
- La CTE de anomalías usa varias ventanas por persona, uniones y un baseline histórico. Un índice por sí solo puede no resolver las fases de ordenamiento y agregación.
- Los índices existentes ya ocupan más del doble que la tabla en este escenario; añadir índices sin evidencia afectaría espacio e inserciones.
- Promedio y mediana difieren bastante en varios endpoints. Para la tesis deben presentarse ambos y conservar máximo/mínimo; una sola media ocultaría la variabilidad.
- `spring.jpa.show-sql=true` y `hibernate.format_sql=true` pueden introducir costo y bloqueo de consola durante el benchmark. No se modificaron para preservar el baseline actual, pero deben mantenerse iguales en la comparación o documentarse como cambio de entorno, no como optimización SQL.
- El endpoint de anomalías paginado calcula el conteo total además del contenido. Debe separarse el tiempo SQL de cada consulta mediante los logs y `EXPLAIN ANALYZE` antes de cambiar su semántica.

## Próximo paso propuesto, pendiente de aprobación

Ejecutar `inspect-postgres.ps1 -Mode Explain` sobre exactamente el mismo rango. Según el plan real se decidirá si conviene ajustar consultas, reutilizar materializaciones, reducir trabajo duplicado del conteo o introducir un índice adicional. No debe implementarse ninguna de esas opciones sin evidencia del plan.
