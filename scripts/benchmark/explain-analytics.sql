\pset pager off
\timing on
\echo 'Rango analizado:' :inicio '->' :fin

\echo 'RESUMEN / ACCESOS POR HORA / ACCESOS POR DIA'
EXPLAIN (ANALYZE, BUFFERS, VERBOSE, FORMAT TEXT)
SELECT COUNT(*) AS total,
       COUNT(*) FILTER (WHERE tipo_evento = 'ENTRADA') AS entradas,
       COUNT(*) FILTER (WHERE tipo_evento = 'SALIDA') AS salidas,
       COUNT(*) FILTER (WHERE estado = 'PERMITIDO') AS permitidos,
       COUNT(*) FILTER (WHERE estado = 'RECHAZADO') AS rechazados,
       COUNT(DISTINCT persona_id) AS personas_unicas
FROM registros_acceso
WHERE fecha_hora >= :'inicio'::timestamp AND fecha_hora < :'fin'::timestamp;

EXPLAIN (ANALYZE, BUFFERS, VERBOSE, FORMAT TEXT)
SELECT EXTRACT(HOUR FROM fecha_hora)::integer AS hora, COUNT(*) AS cantidad
FROM registros_acceso
WHERE fecha_hora >= :'inicio'::timestamp AND fecha_hora < :'fin'::timestamp
GROUP BY EXTRACT(HOUR FROM fecha_hora)
ORDER BY hora;

EXPLAIN (ANALYZE, BUFFERS, VERBOSE, FORMAT TEXT)
SELECT fecha_hora::date AS fecha, COUNT(*) AS cantidad
FROM registros_acceso
WHERE fecha_hora >= :'inicio'::timestamp AND fecha_hora < :'fin'::timestamp
GROUP BY fecha_hora::date
ORDER BY fecha;

\echo 'PERSONAS FRECUENTES'
EXPLAIN (ANALYZE, BUFFERS, VERBOSE, FORMAT TEXT)
SELECT p.id, p.codigo_biometrico, p.nombres, p.apellidos, COUNT(*) AS cantidad
FROM registros_acceso ra
JOIN personas p ON p.id = ra.persona_id
WHERE ra.fecha_hora >= :'inicio'::timestamp AND ra.fecha_hora < :'fin'::timestamp
GROUP BY p.id, p.codigo_biometrico, p.nombres, p.apellidos
ORDER BY cantidad DESC, p.id
LIMIT 10;

\echo 'PATRONES GENERALES (misma estructura principal del repositorio)'
EXPLAIN (ANALYZE, BUFFERS, VERBOSE, FORMAT TEXT)
WITH base AS MATERIALIZED (
    SELECT fecha_hora, tipo_evento FROM registros_acceso
    WHERE fecha_hora >= :'inicio'::timestamp AND fecha_hora < :'fin'::timestamp
), por_hora AS (
    SELECT EXTRACT(HOUR FROM fecha_hora)::integer hora, COUNT(*) total,
           COUNT(*) FILTER (WHERE tipo_evento='ENTRADA') entradas,
           COUNT(*) FILTER (WHERE tipo_evento='SALIDA') salidas
    FROM base GROUP BY EXTRACT(HOUR FROM fecha_hora)
), por_dia AS (
    SELECT fecha_hora::date fecha, EXTRACT(ISODOW FROM fecha_hora)::integer dia_semana, COUNT(*) cantidad
    FROM base GROUP BY fecha_hora::date, EXTRACT(ISODOW FROM fecha_hora)
)
SELECT (SELECT COUNT(*) FROM base),
       (SELECT hora FROM por_hora ORDER BY total DESC, hora LIMIT 1),
       (SELECT fecha FROM por_dia ORDER BY cantidad DESC, fecha LIMIT 1);

\echo 'ANOMALIAS: ejecutar el endpoint y revisar el SQL exacto mostrado por Hibernate; la CTE completa vive en RegistroAccesoRepository.ANOMALIAS_CTE.'
