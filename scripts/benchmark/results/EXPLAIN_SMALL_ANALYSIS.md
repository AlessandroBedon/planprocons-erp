# EXPLAIN SMALL — Diagnóstico de Analytics y anomalías

Diagnóstico ejecutado el 12 de agosto de 2026 sobre PostgreSQL 17.10, con 10.006 registros y el rango inclusivo 2026-01-01 a 2026-08-12 (`fin` SQL exclusivo: 2026-08-13 00:00:00).

No se modificaron consultas de producción, índices, estadísticas, `work_mem`, logging ni configuración PostgreSQL.

## Archivos de evidencia

- `explain_20260812_110453.txt`: consultas analíticas normales.
- `explainanomalies_20260812_110615.txt`: contenido, count y resumen de anomalías por separado.
- `explain-anomalies.sql`: reproducción literal de `ANOMALIAS_CTE` y sus tres terminaciones nativas, con parámetros sustituidos por `psql`.

`RegistroAccesoRepository` usa `nativeQuery=true`; Hibernate no traduce estas consultas a otro dialecto. Sustituye los parámetros `inicio`, `fin`, filtros, límite y desplazamiento. Por eso el SQL de `explain-anomalies.sql` conserva la estructura efectiva de producción. Para esta prueba se usaron filtros `tipo` y `personaId` nulos, `LIMIT 20` y `OFFSET 0`, igual que el endpoint medido.

## Resumen de planes

| Consulta | Planning Time | Execution Time | Nodo más costoso | Scan principal | Índice | Observación |
|---|---:|---:|---|---|---|---|
| Resumen | 26.291 ms | 16.308 ms | Sort para `COUNT DISTINCT` | Seq Scan, 10.006 filas | Ninguno | Quicksort 854 kB; estimación base exacta |
| Accesos por hora | 0.588 ms | 8.291 ms | Seq Scan + HashAggregate | Seq Scan, 10.006 filas | Ninguno | 19 grupos; quicksort 25 kB |
| Accesos por día | 0.288 ms | 7.856 ms | Sort + GroupAggregate | Seq Scan, 10.006 filas | Ninguno | 214 días; quicksort 385 kB |
| Personas frecuentes | 8.112 ms | 9.618 ms | Hash Join + HashAggregate | Seq Scan accesos/personas | Ninguno | 502 personas; top-N 26 kB |
| Patrones representativos | 0.489 ms | 21.540 ms | Materialización y tres lecturas de `base` | Seq Scan, 10.006 filas | Ninguno | 19 horas y 214 días; sin spill |
| Anomalías contenido, limit 20 | 35.639 ms | 146.095 ms | Append de seis ramas y ventanas | Seq Scan, 10.006 filas | Ninguno | Produce 140 anomalías y ordena top 20 |
| Anomalías count | 1.108 ms | 106.546 ms | Append completo antes del Aggregate | Seq Scan, 10.006 filas | Ninguno | Recalcula toda la detección para contar 140 |
| Anomalías resumen | 1.698 ms | 128.293 ms | Append completo + Aggregate FILTER | Seq Scan, 10.006 filas | Ninguno | Recalcula toda la detección y agrega por tipo |

Los buffers fueron exclusivamente `shared hit`; no se observaron lecturas físicas reportadas ni escritura de temporales. La tabla de accesos utilizó 193 buffers y el join con personas elevó el total principal a 202.

## Consultas normales

### Resumen

- `Seq Scan` leyó 10.006 filas; PostgreSQL estimó 10.004.
- El filtro temporal usa directamente `fecha_hora >= inicio AND fecha_hora < fin`.
- El rango cubre prácticamente toda la tabla, por lo que el scan secuencial es racional.
- `COUNT(DISTINCT persona_id)` requirió ordenar 10.006 filas.
- Sort: quicksort, 854 kB, sin disco.
- Resultado: una fila.

### Accesos por hora

- `Seq Scan`: 10.006 filas.
- `EXTRACT(HOUR)` se usa después del filtro, como clave de agrupación; no impide aplicar un índice al rango.
- `HashAggregate`: 19 filas reales frente a 9.996 grupos estimados.
- Una estimación pobre para la expresión de agrupación no produjo un plan costoso con este volumen.
- Hash: 409 kB; sort final: quicksort 25 kB.

### Accesos por día

- `CAST(fecha_hora AS date)` se usa en proyección, agrupación y orden, no en el predicado temporal.
- `Seq Scan`: 10.006 filas.
- Sort de 10.006 claves: quicksort 385 kB.
- `GroupAggregate`: 214 filas reales frente a 9.996 estimadas.

### Personas frecuentes

- `Seq Scan` de 10.006 accesos y 502 personas.
- `Hash Join` produjo 10.006 filas.
- `HashAggregate` produjo 502 personas, exactamente la cardinalidad estimada.
- `top-N heapsort` seleccionó las primeras 10 usando 26 kB.

### Patrones

- `base` fue materializada porque se consulta varias veces.
- Una sola lectura secuencial produjo 10.006 filas.
- Las lecturas posteriores fueron `CTE Scan`, no nuevos accesos a la tabla.
- Los agregados produjeron 19 horas y 214 días.
- Los sorts top-N usaron 25 kB cada uno.

## Anomalías: flujo lógico real

```text
registros_acceso (10.006)
  + personas (502)
        ↓ Hash Join
base materializada (10.006)
        ├─ hora 0–4 → 33
        ├─ hora 5 → 34
        ├─ hora >=22 → 20
        ├─ conteo_5 / WindowAgg 10.006 → 12 repetitivos
        ├─ rechazos_10 / WindowAgg 241 → 6 repetitivos
        └─ entradas_diarias / WindowAgg 4.862
              ├─ primeras entradas → baseline por 500 personas
              └─ baseline suficiente 249 → 35 desviaciones
        ↓ UNION ALL / Append
140 anomalías
```

### CTE `base`

- Se materializa porque tiene múltiples referencias.
- Se calcula una vez por sentencia mediante `Hash Join`.
- Contenido: 39.163 ms en el primer plan; count: 12.811 ms; resumen: 30.585 ms.
- Contiene campos de persona y dos expresiones temporales para todas las filas.
- Luego se vuelve a leer en cada rama con `CTE Scan`.

### `conteo_5`

- PostgreSQL la inlinéa porque tiene una sola referencia.
- `COUNT(*) OVER (PARTITION BY persona_id ORDER BY fecha_hora RANGE 5 minutes)`.
- Ordena 10.006 filas por persona y fecha.
- Sort: quicksort, 775 kB.
- `WindowAgg` del plan de contenido: 9.114–32.953 ms.
- Generó conteos para 10.006 filas y eliminó 9.994; quedaron 12 anomalías.

### `rechazos_10`

- También se inlinéa.
- Filtra primero 241 rechazados de 10.006 filas.
- `COUNT OVER`, particionado por persona y ordenado por fecha, ventana de 10 minutos.
- Sort: quicksort 34 kB.
- Quedaron 6 anomalías después de descartar 235 filas.

### `entradas_diarias`

- Se materializa porque la usan las entradas candidatas y `baseline`.
- Filtra 4.862 entradas permitidas.
- `ROW_NUMBER() OVER (PARTITION BY persona_id, fecha_hora::date ORDER BY fecha_hora, id)`.
- Sort: quicksort 753 kB.
- El `WindowAgg` tardó 13.589 ms en contenido, 27.995 ms en count y 16.933 ms en resumen.

### `baseline`

- Se inlinéa dentro de la rama de desviación.
- Consume primeras entradas y agrupa por persona.
- 500 personas tuvieron primeras entradas; 249 superaron el mínimo de 10 días.
- Se materializa el resultado del agregado dentro del merge join para reutilizarlo durante el join.
- La rama produjo 35 desviaciones después de eliminar 3.073 combinaciones por el filtro de diferencia mayor a 120 minutos.

### `anomalias`

- Al tener una sola referencia, PostgreSQL la inlinéa como un `Append` de seis ramas.
- No existe una tabla o resultado persistente de anomalías.
- Cada sentencia —contenido, count o resumen— construye nuevamente las 140 anomalías.
- Para contenido, las 140 filas se ordenan por fecha, id y tipo mediante `top-N heapsort` de 29 kB para devolver 20.

## PAGE + COUNT

El endpoint `GET /api/analisis/anomalias` ejecuta explícitamente dos métodos de repositorio:

1. `detectarAnomalias(...)` para contenido.
2. `contarAnomalias(...)` para total.

Cada método es una sentencia SQL separada y cada sentencia vuelve a ejecutar `base`, ventanas, baseline y las seis ramas.

Costo PostgreSQL medido por separado:

- Contenido: 146.095 ms.
- Count: 106.546 ms.
- Trabajo SQL combinado mínimo del endpoint: aproximadamente 252.641 ms, sin incluir JDBC, mapeo, Spring ni serialización.

El `LIMIT 20` no evita calcular las anomalías: el orden global necesita formar las 140 candidatas antes de seleccionar las 20 más recientes.

## Sorts y `work_mem`

No hubo spills a disco.

| Operación | Método | Memoria máxima observada |
|---|---|---:|
| Distinct resumen | quicksort | 854 kB |
| Conteo repetitivo 5 min | quicksort | 775 kB |
| Entradas diarias | quicksort | 753 kB |
| Accesos por día | quicksort | 385 kB |
| Rechazos 10 min | quicksort | 34 kB |
| Top 20 anomalías | top-N heapsort | 29 kB |

Con 10k registros no existe evidencia para aumentar `work_mem`. En 100k/500k debe volver a revisarse.

## Cardinalidad

La estimación del rango base es excelente: 10.004 estimadas frente a 10.006 reales. Esto sugiere estadísticas suficientes para `fecha_hora` y no justifica ejecutar `ANALYZE` como corrección inmediata.

Errores relevantes posteriores:

- Hora tardía: 3.335 estimadas frente a 20 reales.
- Accesos repetitivos: 3.335 frente a 12.
- Primeras entradas (`posicion=1`): alrededor de 24 estimadas frente a 4.729 observadas por la lectura usada en baseline.
- Grupos con al menos 10 días: 8 estimados frente a 249.
- Desviaciones: 1 estimada frente a 35.
- Anomalías totales del `Append`: 6.851 estimadas frente a 140 reales.
- Hora y día normales también estiman casi una fila distinta por registro: 9.996 frente a 19 horas y 214 días.

Estas diferencias provienen principalmente de expresiones, resultados de ventanas y filtros correlacionados que las estadísticas ordinarias no modelan bien. Un `ANALYZE` podría refrescar estadísticas base, pero no se espera que por sí solo corrija selectividad de `ROW_NUMBER`, ventanas móviles o combinaciones de hora/estado/tipo.

## Índices utilizados

Ninguno de los siete índices de `registros_acceso` fue utilizado en estos planes.

Esto no demuestra que sean inútiles. El rango incluye prácticamente el 100% de la tabla, y un `Seq Scan` es apropiado. Los índices compuestos siguen siendo relevantes para consultas selectivas por persona, dispositivo, tipo o estado y para unicidad.

No existe evidencia en este escenario para crear otro índice. En particular:

- El filtro principal usa la columna `fecha_hora` sin función, por lo que `idx_registros_fecha_hora` es elegible.
- `EXTRACT` y cast a fecha aparecen después del filtro, en agrupaciones, particiones o clasificación.
- Las ramas de hora operan sobre `base` ya materializada, de modo que un índice de expresión por hora no sería accesible dentro de esos `CTE Scan`.

## Diferencia entre baseline HTTP y PostgreSQL

El baseline HTTP registró:

- Resumen de anomalías: promedio 17.661 s.
- Página de anomalías: promedio 23.264 s.

Los planes directos registraron 128.293 ms para resumen y 252.641 ms combinados para página + count. Una medición HTTP de control posterior dio 115.068 ms para resumen y 133.495 ms para el endpoint paginado completo.

Por tanto, la lentitud de decenas de segundos no es reproducible actualmente ni está explicada por el plan PostgreSQL. Es compatible con una condición transitoria del proceso o entorno, por ejemplo bloqueo/saturación de salida al usar `spring.jpa.show-sql=true`, calentamiento, pausa de JVM o carga externa. Esto es una inferencia: no se capturó evidencia suficiente para atribuirla definitivamente a una de esas causas.

Antes de comparar una optimización debe repetirse el baseline en un entorno controlado y conservar iguales el estado del logging, proceso Spring y carga del equipo.

## Ranking de cuellos de botella

### CRÍTICO

1. **Duplicación de página y count.** El endpoint paginado ejecuta dos sentencias independientes que reconstruyen toda la detección. Evidencia: 146.095 + 106.546 ms.
2. **Variabilidad HTTP no explicada por PostgreSQL.** El baseline alcanzó 15–44 s, mientras SQL directo y HTTP posterior quedaron en centenas de milisegundos. Sin controlar esto, una comparación antes/después sería metodológicamente inválida.

### ALTO

3. **Seis lecturas lógicas y tres ventanas sobre resultados derivados.** `base` se calcula una vez por sentencia, pero cada rama la recorre y las ventanas ordenan 10.006, 4.862 y 241 filas.
4. **Cardinalidades derivadas muy incorrectas.** El planner espera 6.851 anomalías y obtiene 140; espera 8 baselines y obtiene 249.

### MEDIO

5. **CTE ancha.** `base` transporta datos de identidad que count/resumen no necesitan y los conserva durante scans y ventanas.
6. **Orden global previo al LIMIT.** Es necesario bajo la semántica actual; con 140 resultados solo usa 29 kB.

### BAJO

7. **Scans secuenciales en este escenario.** Son apropiados para un rango que cubre casi toda la tabla.
8. **Memoria de sorts.** Todos permanecieron en RAM; no hay evidencia de presión de `work_mem` con 10k.

## Máximo cinco optimizaciones propuestas

### 1. C — Paginación/count: ejecutar la detección una sola vez

- Problema: contenido y count reconstruyen independientemente toda la CTE.
- Evidencia: 146.095 ms de contenido y 106.546 ms de count.
- Cambio propuesto: devolver contenido y total desde una sola sentencia, por ejemplo con total asociado al resultado completo antes del `LIMIT`, resolviendo correctamente el caso de página vacía.
- Beneficio esperado: eliminar cualitativamente una ejecución completa de ventanas y ramas.
- Riesgo: complejidad del mapeo y tratamiento de offsets fuera de rango.
- Semántica: debe conservar exactamente contenido, orden y `totalElements`.
- INSERT: sin impacto.

### 2. A — Consulta: usar una base analítica estrecha

- Problema: count y resumen transportan nombres, apellidos y código aunque no los devuelven.
- Evidencia: `base` contiene campos de persona para las tres variantes y se lee repetidamente.
- Cambio: calcular anomalías solo con id/persona/fecha/tipo/estado; unir `personas` únicamente al final de la consulta de contenido.
- Beneficio esperado: menos bytes en materialización, sorts, ventanas y scans.
- Riesgo: reescritura cuidadosa de proyecciones.
- Semántica: ninguna si el join final conserva la misma persona.
- INSERT: sin impacto.

### 3. A — Consulta: evitar scans independientes para categorías horarias simples

- Problema: nocturno, temprano y tardío recorren `base` por separado.
- Evidencia: cada rama elimina alrededor de 9.970 de 10.006 filas.
- Cambio: clasificar las categorías horarias en una sola lectura lateral o estructura equivalente, manteniendo `UNION ALL` solo donde un registro pueda generar varias anomalías.
- Beneficio esperado: reducir recorridos y evaluación repetida.
- Riesgo: alterar accidentalmente multiplicidad de anomalías.
- Semántica: riesgo medio; requiere pruebas exactas de tipos y conteos.
- INSERT: sin impacto.

### 4. D — Configuración experimental: controlar logging SQL

- Problema: el baseline HTTP no coincide con el costo SQL ni con la medición HTTP posterior.
- Evidencia: decenas de segundos frente a 100–250 ms.
- Cambio: repetir una medición controlada con salida capturada y luego una prueba A/B separada de `show-sql`, documentándola como configuración, no como índice/consulta.
- Beneficio esperado: distinguir costo de observabilidad de costo analítico.
- Riesgo: perder diagnóstico si se desactiva sin alternativa.
- Semántica e INSERT: sin cambio lógico; puede reducir overhead global.

### 5. E — Arquitectura futura: persistir resultados analíticos solo si 100k/500k lo justifican

- Problema: toda consulta recalcula ventanas históricas sobre el rango completo.
- Evidencia: el trabajo crece con filas por persona y rango, aunque 10k todavía ejecuta en cientos de milisegundos SQL.
- Cambio: tabla/materialización incremental de anomalías o resumen analítico, actualizada al ingerir o en proceso batch.
- Beneficio esperado: lecturas predecibles para volúmenes grandes.
- Riesgo: consistencia, reprocesamiento, idempotencia y mayor arquitectura.
- Semántica: debe definir cuándo una anomalía queda consolidada.
- INSERT: impacto directo o diferido según diseño; no recomendable en V1 sin medir 100k.

### B — Índices

No se propone un índice nuevo con la evidencia SMALL actual. El cuello está en procesamiento repetido y ventanas sobre casi toda la tabla, no en búsquedas selectivas demostrablemente costosas.

## Primera intervención recomendada

Antes de cambiar producción, repetiría el baseline SMALL bajo condiciones controladas para resolver la discrepancia HTTP/SQL. Como primera optimización de código, implementaría la unificación de contenido + count, porque es el trabajo duplicado más directamente demostrado, conserva el modelo analítico y no penaliza INSERT.
