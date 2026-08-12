# Optimización de anomalías: página + total

## Resultado

La página y `totalElements` se obtienen ahora mediante una sola consulta analítica. El CTE de detección de anomalías se ejecuta una vez; `COUNT(*) OVER ()` incorpora el total a las filas paginadas. Cuando la página está vacía o fuera de rango, una fila técnica devuelve el total sin volver a construir ni ejecutar el CTE analítico.

La consulta separada `contarAnomalias` fue eliminada.

## Compatibilidad funcional

Se compararon hashes SHA-256 del objeto `data` antes y después para seis escenarios:

- primera página;
- segunda página;
- página fuera de rango;
- filtro por tipo;
- filtro por persona;
- período sin registros.

Los seis hashes coinciden. El detalle reproducible está en `page_count_validation/before_manifest.csv` y `page_count_validation/after_manifest.csv`.

## Diseño SQL

1. `ANOMALIAS_CTE` conserva sin cambios las reglas de detección.
2. `filtradas AS MATERIALIZED` aplica la variante exacta de filtros requerida.
3. `COUNT(*) OVER ()` calcula el total sobre el conjunto filtrado.
4. `pagina AS MATERIALIZED` conserva el orden `fecha_hora DESC, id DESC, tipo` y aplica `LIMIT/OFFSET`.
5. El `UNION ALL` agrega únicamente una fila técnica cuando `pagina` no contiene filas. Esta fila lee `filtradas` ya materializada; no repite la detección.

Se usan cuatro variantes estáticas para: sin filtros, tipo, persona y tipo+persona. Esto evita predicados opcionales ambiguos para el planificador y mantiene parámetros enlazados.

PostgreSQL cambió a un plan genérico después del umbral de sentencias preparadas. Ese plan elegía nested loops con estimaciones de cardinalidad inadecuadas y elevaba algunas llamadas a 15–17 segundos. Antes de la consulta analítica se aplica, dentro de la transacción, `set_config('enable_nestloop', 'off', true)`. Es un ajuste local y reversible al finalizar la transacción; no modifica `application.properties` ni la configuración global de PostgreSQL. La petición ejecuta una sola consulta de datos analíticos más esta sentencia de control del planificador.

## EXPLAIN ANALYZE posterior

Archivo: `optimized_page_count_explain.txt`.

- Planning Time: 17.031 ms
- Execution Time: 82.248 ms
- Buffers compartidos: 208 hits en el nodo superior
- Resultado: 20 filas de contenido más metadatos
- Método principal para los cruces voluminosos: hash joins

Comparación con el diagnóstico anterior:

- contenido anterior: 146.095 ms
- count anterior: 106.546 ms
- total de las dos ejecuciones: 252.641 ms
- consulta analítica única actual: 82.248 ms
- reducción del tiempo SQL acumulado: 67.444 %

## Benchmark HTTP estabilizado

Condiciones: logging SQL desactivado, una llamada de calentamiento y siete mediciones sobre la página 0 de tamaño 20.

| Métrica | Baseline | Optimizado |
|---|---:|---:|
| Promedio | 117.895 ms | 73.048 ms |
| Mínimo | 113.913 ms | 68.798 ms |
| Mediana | 117.548 ms | 71.837 ms |
| Máximo | 121.931 ms | 79.684 ms |

La mejora media HTTP fue 38.039 %. Las mediciones individuales están en `baseline_logging_off_raw.csv` y `optimized_page_count_raw.csv`.

Se conservaron por separado las series preliminares/noisy para documentar el problema del plan genérico y la variación de calentamiento; no forman parte de la comparación oficial.

## Alcance

No se añadieron índices, no se cambió configuración global, no se modificaron entidades ni seguridad y no se ejecutó todavía la fase de 100.000 registros.
