# Benchmark de escalabilidad final

Este documento consolida SMALL, MEDIUM y LARGE. El detalle reproducible, metodología, EXPLAIN, hashes y limitaciones se encuentra en `BENCHMARK_SMALL.md`/baseline disponible, `BENCHMARK_MEDIUM.md`, `MEDIUM_ANOMALY_OPTIMIZATION_V21.md` y `BENCHMARK_LARGE.md`.

## Metodología

Tres volúmenes reales: 10.006, 100.000 y 500.000 registros; mismo rango `2026-01-01`–`2026-08-12`; un warm-up, siete ejecuciones secuenciales, logging SQL OFF y mismo equipo. LARGE conserva V2.1 y no incorpora optimizaciones nuevas.

## Latencia

| Endpoint | SMALL ms | MEDIUM ms | LARGE ms |
|---|---:|---:|---:|
| resumen | 85,718 | 57,495 | 52,029 |
| accesos por hora | 97,985 | 46,878 | 53,260 |
| accesos por día | 261,809 | 126,245 | 458,943 |
| personas frecuentes | 88,562 | 142,543 | 389,501 |
| patrones | 243,277 | 411,133 | 1.113,551 |
| anomalías resumen | 74,340 | 741,393 | 4.412,749 |
| anomalías página | 73,048 | 653,568 | 4.066,809 |

## Ingestión y almacenamiento

La carga MEDIUM→LARGE insertó 400.000 filas en 85,676 s (4.668,75 reg/s). El almacenamiento total pasó aproximadamente de 4,9 MB a 48,4 MB y 236,1 MB; el costo total por fila bajó de ~500 a ~472 bytes.

## EXPLAIN y anomalías

PostgreSQL adoptó paralelismo para personas frecuentes y scans secuenciales para varios rangos que cubren casi toda la tabla. Patrones usa temporales. En anomalías, 16 MB local ya no evita external merge a 500k: página usa 27.436/12.012 bloques temp y 3.938,747 ms SQL; resumen usa 31.646/12.012 y 6.890,647 ms. El control de nested loops sigue estable.

## Hallazgos y limitaciones

Analytics general escala de forma buena o moderada. Las anomalías bajo demanda son el límite demostrado: 4–4,4 s HTTP. Todos los hashes fueron estables y no hubo errores/timeouts. La prueba es local, secuencial y no mide concurrencia.

La evidencia es suficiente para cerrar el experimento del diseño actual. Una V3 solo se recomienda si el objetivo posterior exige interacción operativa fluida con anomalías sobre 500k: persistencia incremental, reglas versionadas y reconstrucción histórica.
