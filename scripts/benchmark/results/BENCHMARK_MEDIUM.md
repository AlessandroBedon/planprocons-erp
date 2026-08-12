# Benchmark MEDIUM — 100.000 registros

## Dataset

El inventario previo registró 10.006 accesos: 10.000 sintéticos y 6 preexistentes. Se añadieron únicamente 89.994 accesos mediante el generador batch existente, sin eliminar ni reiniciar datos. El volumen final real y usado como `DatasetSize` fue 100.000.

| Propiedad | Valor |
|---|---:|
| Registros totales | 100.000 |
| Registros sintéticos | 99.994 |
| Personas | 1.002 |
| Personas sintéticas | 1.000 |
| Dispositivos | 7 |
| Dispositivos sintéticos | 5 |
| Fecha mínima | 2026-01-01 02:28:08 |
| Fecha máxima | 2026-08-12 09:10:00 |
| Tabla `registros_acceso` | 15.761.408 bytes (aprox. 15 MB) |
| Índices de la tabla | 32.604.160 bytes (aprox. 31 MB) |
| Total tabla + índices | 48.398.336 bytes (aprox. 46 MB) |
| Base de datos completa | 57.751.219 bytes |

En SMALL, tabla + índices ocupaban aproximadamente 4.896 kB; MEDIUM ocupa aproximadamente 46 MB. El crecimiento de almacenamiento es cercano al crecimiento del volumen.

## Ingestión 10k → 100k

Se conservaron seed `12345`, distribución, perfiles, anomalías, prefijo `SYN-`, idempotencia y lotes de 5.000. El lote adicional cubrió `2026-01-01` a `2026-07-31`, el mismo período generativo reconstruido del lote SMALL; no fue necesario extender fechas.

| Métrica | Resultado |
|---|---:|
| Registros antes | 10.006 |
| Registros solicitados/agregados | 89.994 |
| Registros finales | 100.000 |
| Tiempo interno del generador | 21.150 ms |
| Tiempo HTTP observado | 21.515,880 ms |
| Throughput | 4.255,04 registros/s |

El tiempo de ingestión se mantuvo separado de la latencia analítica.

## Metodología

- Rango analítico: `2026-01-01` a `2026-08-12`, inclusivo por fecha.
- DatasetSize: 100.000 real.
- Un warm-up por endpoint y siete ejecuciones medidas.
- Ejecución secuencial sobre la misma máquina y backend.
- SQL logging desactivado.
- Cachés del sistema operativo y PostgreSQL no fueron vaciadas.
- Backend Spring Boot/Java 21 en `localhost:8081` y PostgreSQL 17.10 local.
- La optimización vigente de anomalías permaneció activa: consulta analítica única, `COUNT(*) OVER()` y control transaccional del planificador.

Por tanto, las cifras incluyen HTTP local, seguridad, JPA/JDBC y serialización; no son exclusivamente tiempo SQL.

## Resultados MEDIUM

| Endpoint | Min ms | Máx ms | Promedio ms | Mediana ms | HTTP |
|---|---:|---:|---:|---:|---:|
| `/api/dashboard/resumen` | 42,879 | 99,507 | 57,495 | 52,021 | 200 |
| `/api/dashboard/accesos-por-hora` | 40,159 | 54,610 | 46,878 | 48,490 | 200 |
| `/api/dashboard/accesos-por-dia` | 110,350 | 139,058 | 126,245 | 126,103 | 200 |
| `/api/dashboard/personas-frecuentes` | 110,918 | 182,192 | 142,543 | 145,474 | 200 |
| `/api/analisis/patrones` | 370,478 | 458,995 | 411,133 | 395,919 | 200 |
| `/api/analisis/anomalias/resumen` | 969,468 | 1.348,416 | 1.146,455 | 1.102,258 | 200 |
| `/api/analisis/anomalias` | 959,783 | 1.110,420 | 1.026,629 | 1.015,467 | 200 |

No hubo errores HTTP. La mayor separación máximo/mediana apareció en `anomalias/resumen` (246,158 ms) y en el resumen diario por una primera medición más fría; ninguna serie presentó el patrón catastrófico de 15–17 segundos.

## Estabilidad funcional y hashes

Cada endpoint produjo un único SHA-256 funcional durante sus siete ejecuciones:

| Endpoint | SHA-256 |
|---|---|
| resumen | `110685a38f028615334ee80dd6d784a8c3b9567a9e88f5977439f2d19455c737` |
| accesos por hora | `a36986bf92b6745061de307f5f243c608ac543129159c7519fb1105dd720dadf` |
| accesos por día | `80e79e089fe0bc99b129de153a006939f18f3fb390989c0c092b1f783b848a7f` |
| personas frecuentes | `610621f43467a8cf5d806a45173c5fa7488c85bc61c60c5d3a9bf236b06beb4a` |
| patrones | `dc1b5072602f982f88c094d3c66bbaec09a629885e5d63c32171f6f3f7a7ac76` |
| anomalías resumen | `fcee3fb6f8ef91b1bbd8fbb5b3eacf06f1f8ba3bbe161c422dc1c8e2fdfc9b9f` |
| anomalías página | `ec77de2e55fcd48fc8987417fdeea836d769a906d4a1535e6c318c2162bc2494` |

Los hashes MEDIUM no deben coincidir con SMALL porque el dataset cambió; la estabilidad relevante es su igualdad entre las siete ejecuciones MEDIUM.

## Comparación SMALL/MEDIUM

Se utilizaron los CSV SMALL existentes. Para la página de anomalías se usó explícitamente el SMALL optimizado estable de 73,048 ms, no el baseline contaminado. Para el resumen de anomalías se usó el logging-off estable de 74,340 ms. Los demás endpoints provienen de `baseline_summary.csv`; su alta variabilidad SMALL limita la interpretación de factores menores que uno.

| Endpoint | SMALL avg ms | MEDIUM avg ms | Cambio absoluto ms | Factor MEDIUM/SMALL |
|---|---:|---:|---:|---:|
| accesos por día | 261,809 | 126,245 | -135,564 | 0,482× |
| accesos por hora | 97,985 | 46,878 | -51,107 | 0,478× |
| anomalías página | 73,048 | 1.026,629 | +953,581 | 14,054× |
| anomalías resumen | 74,340 | 1.146,455 | +1.072,115 | 15,422× |
| patrones | 243,277 | 411,133 | +167,856 | 1,690× |
| personas frecuentes | 88,562 | 142,543 | +53,981 | 1,610× |
| resumen | 85,718 | 57,495 | -28,223 | 0,671× |

Clasificación basada en multiplicar el volumen aproximadamente 9,994×:

- Escalado bueno: resumen, accesos por hora, accesos por día, personas frecuentes y patrones. Sus factores son muy inferiores al crecimiento del dataset y las latencias permanecen por debajo de 0,5 s.
- Escalado problemático: ambas consultas de anomalías. Sus factores superan incluso el crecimiento del volumen y rondan/superan 1 s.
- No se identificó un caso intermedio claro en este escenario.

El endpoint más afectado por factor fue `anomalias/resumen` (15,422×). El mejor factor numérico fue `accesos-por-hora` (0,478×), aunque debe interpretarse junto con el ruido elevado del baseline SMALL.

## EXPLAIN ANALYZE MEDIUM

Cambios principales frente a SMALL:

- Resumen: SMALL hacía `Seq Scan` y terminaba en 16,308 ms; MEDIUM eligió `Index Scan` sobre `idx_registros_persona_fecha`, 100.448 buffers hit y 121,499 ms.
- Por hora: cambió de `Seq Scan` a `Index Only Scan` sobre `idx_registros_fecha_hora`, sin heap fetches; 90,444 ms.
- Por día: también `Index Only Scan`, sin heap fetches; 59,390 ms.
- Personas frecuentes: mantuvo `Seq Scan + Hash Join + HashAggregate`; 91,284 ms.
- Patrones: mantuvo CTE/lectura secuencial y agregaciones hash, pero comenzó a usar temporales (`temp read=624`, `written=312`); 285,946 ms.
- Anomalías optimizadas: conservó hash joins y evitó nested loops defectuosos, pero pasó de 82,248 ms SQL en SMALL a 3.226,940 ms en este EXPLAIN frío. Los `WindowAgg` requirieron external merge de 5.136 kB y 3.336 kB; el plan acumuló `temp read=10.591`, `written=3.385`.

El EXPLAIN de anomalías fue sensiblemente más lento que el HTTP caliente (~1 s), coherente con cachés/overhead de `EXPLAIN (ANALYZE, BUFFERS, VERBOSE)` y temporales. Se reportan ambos sin sustituir uno por el otro.

## Riesgos y candidatos posteriores — no implementados

1. Reducir el ancho de las filas materializadas por los CTE de anomalías; actualmente los sorts/window cargan columnas textuales que no todas las etapas necesitan.
2. Reestructurar las categorías horarias para evitar múltiples lecturas completas de `base`, preservando que un acceso pueda pertenecer a más de una anomalía.
3. Evaluar una fase controlada de memoria de trabajo para los sorts de anomalías, dado el external merge demostrado; no cambiarla sin benchmark aislado.
4. Evaluar materialización/incrementalización de anomalías para cargas mayores si el requisito admite datos precalculados.
5. Revisar el `Index Scan` elegido para el resumen total, que leyó 100.448 buffers; comparar explícitamente con un scan secuencial en una fase de diagnóstico, sin forzar configuración global.

## Preparación para LARGE

El sistema es funcional y estable con 100.000 registros, pero no se recomienda ejecutar todavía LARGE como benchmark oficial. Primero conviene revisar el cuello de botella demostrado en anomalías y acordar una fase de optimización MEDIUM: a 500.000 registros, los sorts externos y múltiples recorridos del CTE probablemente elevarán sustancialmente latencia y uso temporal. Esta recomendación no implica que se haya aplicado ninguna optimización nueva.
