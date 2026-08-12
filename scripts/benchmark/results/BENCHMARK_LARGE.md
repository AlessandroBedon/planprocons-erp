# Benchmark LARGE — 500.000 registros

## Dataset e ingestión

El inventario previo confirmó 100.000 registros. Se generaron únicamente los 400.000 faltantes mediante el generador batch existente, seed `12345`, rango generativo `2026-01-01`–`2026-07-31`, lotes de 5.000 e identificador idempotente `3F8822F5AA32`. No se eliminaron datos ni se cambiaron distribuciones.

| Métrica | Valor |
|---|---:|
| Registros antes | 100.000 |
| Registros añadidos | 400.000 |
| Registros finales | 500.000 |
| Sintéticos finales | 499.994 |
| Tiempo interno de generación | 85.676 ms |
| Tiempo HTTP de generación | 86.063,846 ms |
| Throughput | 4.668,75 registros/s |
| Personas | 2.002 |
| Dispositivos | 7 |
| Fecha mínima | 2026-01-01 02:28:08 |
| Fecha máxima | 2026-08-12 09:10:00 |

## Metodología

- DatasetSize real: 500.000.
- Rango analítico: `2026-01-01`–`2026-08-12`.
- Un warm-up y siete mediciones por endpoint.
- Benchmark secuencial, SQL logging OFF, misma máquina/backend/cliente.
- Timeouts existentes sin ampliación.
- Anomalías V2.1: consulta única, `COUNT(*) OVER()`, base estrecha, join tardío, `enable_nestloop=off` y `work_mem=16MB`, ambos locales.
- Windows 11/NT 10.0.26200 x64; CPU Intel64 Family 6 Model 142, 4 procesadores lógicos; Java 21.0.12; PostgreSQL 17.10.
- Los permisos del sistema impidieron consultar por WMI el modelo y RAM física; esta limitación se registra sin inventar datos.

## Resultados HTTP LARGE

| Endpoint | Mínimo ms | Mediana ms | Máximo ms | Promedio ms | HTTP |
|---|---:|---:|---:|---:|---:|
| resumen | 43,104 | 50,739 | 69,259 | 52,029 | 200 |
| accesos por hora | 36,902 | 40,187 | 135,492 | 53,260 | 200 |
| accesos por día | 442,938 | 452,073 | 493,236 | 458,943 | 200 |
| personas frecuentes | 368,683 | 381,920 | 434,985 | 389,501 | 200 |
| patrones | 1.092,389 | 1.119,292 | 1.135,692 | 1.113,551 | 200 |
| anomalías resumen | 3.663,210 | 4.543,013 | 5.031,857 | 4.412,749 | 200 |
| anomalías página | 3.671,039 | 4.122,212 | 4.384,380 | 4.066,809 | 200 |

No hubo errores ni timeouts. El máximo de accesos por hora se separó de la mediana por una única ejecución de 135,492 ms; anomalías resumen presentó la mayor amplitud absoluta (1.368,647 ms), pero sin la degradación abrupta del antiguo plan genérico.

## Anomalías LARGE

- Registros analizados: 500.000.
- Total: 17.211.
- Nocturnos: 1.664.
- Tempranos: 1.544.
- Tardíos: 1.000.
- Accesos repetitivos: 11.097.
- Rechazos repetitivos: 643.
- Desviaciones de horario: 1.263.
- Página 0: 20 elementos; 861 páginas; `totalElements=17.211`.

## EXPLAIN LARGE

Planes generales:

- resumen: `Index Scan idx_registros_persona_fecha`, 500.000 filas, 763,117 ms, 497.065 buffers hit y 5.241 read;
- por hora: `Seq Scan + HashAggregate`, 676,273 ms;
- por día: `Seq Scan + external merge`, 1.023,303 ms, temp 735/737;
- personas frecuentes: `Parallel Seq Scan + Hash Join + Partial/Finalize HashAggregate`, 483,481 ms;
- patrones: `Seq Scan`, CTE y `HashAggregate`; 3.557,434 ms, temp 3.114/1.557.

Anomalías con 16 MB local:

| Consulta | Execution Time | Temp read | Temp written | Sorts voluminosos |
|---|---:|---:|---:|---|
| página | 3.938,747 ms | 27.436 | 12.012 | external merge 17.432/16.648 kB |
| resumen | 6.890,647 ms | 31.646 | 12.012 | external merge 17.432/16.648 kB |

Con 500k, 16 MB dejó de ser suficiente para mantener todos los sorts/materializaciones en memoria. Reaparecieron temporales y external merge. No se elevó a 32 MB. `enable_nestloop=off` continuó efectivo: no aparece `Nested Loop`; predominan hash y merge joins. No se añadieron índices.

## Almacenamiento

| Escenario | Registros | Tabla | Índices | Total | Bytes tabla/registro | Bytes total/registro |
|---|---:|---:|---:|---:|---:|---:|
| SMALL | 10.006 | 1.581.056 B | 3.399.680 B | 5.013.504 B | 158,011 | 500,050 |
| MEDIUM | 100.000 | 15.761.408 B | 32.604.160 B | 48.398.336 B | 157,614 | 483,983 |
| LARGE | 500.000 | 78.774.272 B | 157.237.248 B | 236.060.672 B | 157,549 | 472,121 |

El almacenamiento crece casi linealmente; la tabla mantiene aproximadamente 158 bytes por registro y el total por registro disminuye ligeramente al amortizar metadatos/páginas.

## Ingestión

| Transición | Insertados | Tiempo | Throughput |
|---|---:|---:|---:|
| SMALL→MEDIUM | 89.994 | 21,150 s | 4.255,04 reg/s |
| MEDIUM→LARGE | 400.000 | 85,676 s | 4.668,75 reg/s |

No existe una medición persistida equivalente de la generación inicial SMALL, por lo que no se inventó.

## Estabilidad funcional

Las siete ejecuciones de cada endpoint produjeron un único hash y HTTP 200:

| Endpoint | SHA-256 |
|---|---|
| resumen | `110685a38f028615334ee80dd6d784a8c3b9567a9e88f5977439f2d19455c737` |
| accesos por hora | `a36986bf92b6745061de307f5f243c608ac543129159c7519fb1105dd720dadf` |
| accesos por día | `714d48e1b0e0c0155da06cc7a8f3358efc4cd80ec7cc68cc78fb9dee5094334d` |
| personas frecuentes | `a43933f9bc288a16dddda57f79ff2f5d7a07bf728d3137bc83380e37efc707d5` |
| patrones | `3997245e79b63d5c831538111dfddc07a41db8594b31aa1ce330781f59b487ca` |
| anomalías resumen | `5339bd6f6de453df6cd12dcbc7c1a001db73dd77972d8d5b23c3f900c580c2bd` |
| anomalías página | `d7f1e1157f0bcef024960a4894783699e375e2416a1fb093c321460c380c0037` |

## Escalabilidad

El volumen real creció 9,994× SMALL→MEDIUM, 5× MEDIUM→LARGE y 49,970× SMALL→LARGE.

| Endpoint | SMALL avg | MEDIUM avg | LARGE avg | S→M | M→L | S→L | Evaluación LARGE |
|---|---:|---:|---:|---:|---:|---:|---|
| resumen | 85,718 | 57,495 | 52,029 | 0,671× | 0,905× | 0,607× | Bueno |
| accesos por hora | 97,985 | 46,878 | 53,260 | 0,478× | 1,136× | 0,544× | Bueno |
| accesos por día | 261,809 | 126,245 | 458,943 | 0,482× | 3,635× | 1,753× | Moderado |
| personas frecuentes | 88,562 | 142,543 | 389,501 | 1,610× | 2,733× | 4,398× | Bueno |
| patrones | 243,277 | 411,133 | 1.113,551 | 1,690× | 2,708× | 4,577× | Moderado |
| anomalías resumen | 74,340 | 741,393 | 4.412,749 | 9,973× | 5,952× | 59,359× | Problemático |
| anomalías página | 73,048 | 653,568 | 4.066,809 | 8,947× | 6,222× | 55,673× | Problemático |

“Bueno” significa que la latencia creció muy por debajo del volumen y permaneció operativamente baja; “moderado” indica crecimiento sublineal pero latencia cercana/superior a 0,5–1 s; “problemático” indica crecimiento igual o superior al volumen MEDIUM→LARGE y latencias de varios segundos.

El mejor escalado fue resumen (factor M→L 0,905×). El peor fue anomalías página por factor M→L (6,222×); anomalías resumen tuvo la mayor latencia absoluta.

## Limitaciones y decisión

- Prueba local, secuencial y con cachés calientes/no controladas; no mide concurrencia.
- HTTP y EXPLAIN no son capas equivalentes.
- El baseline SMALL general tenía variabilidad alta; anomalías usa las referencias optimizadas controladas exigidas.
- No se pudo inventariar RAM/modelo por restricciones WMI.

El sistema actual es suficiente para cerrar la evaluación experimental y demostrar tanto escalabilidad aceptable en Analytics general como el límite de las anomalías calculadas bajo demanda. Para uso operativo interactivo con 500k sí se recomienda una V3 posterior: anomalías incrementales/persistidas con reglas versionadas y reconstrucción histórica controlada. No se implementó durante este benchmark.
