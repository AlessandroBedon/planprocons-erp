\pset pager off
\timing on
\echo 'Rango analizado:' :inicio '->' :fin

\echo 'ANOMALIAS_CONTENIDO_LIMIT_20'
EXPLAIN (ANALYZE, BUFFERS, VERBOSE, FORMAT TEXT)
WITH base AS (
    SELECT ra.id, ra.persona_id, ra.fecha_hora, ra.tipo_evento, ra.estado,
           p.codigo_biometrico, p.nombres, p.apellidos,
           EXTRACT(HOUR FROM ra.fecha_hora)::integer AS hora,
           EXTRACT(EPOCH FROM ra.fecha_hora::time) / 60.0 AS minuto_dia
    FROM registros_acceso ra INNER JOIN personas p ON p.id = ra.persona_id
    WHERE ra.fecha_hora >= :'inicio'::timestamp AND ra.fecha_hora < :'fin'::timestamp
), conteo_5 AS (
    SELECT id, COUNT(*) OVER (
        PARTITION BY persona_id ORDER BY fecha_hora
        RANGE BETWEEN INTERVAL '5 minutes' PRECEDING AND CURRENT ROW
    ) AS cantidad FROM base
), rechazos_10 AS (
    SELECT id, COUNT(*) OVER (
        PARTITION BY persona_id ORDER BY fecha_hora
        RANGE BETWEEN INTERVAL '10 minutes' PRECEDING AND CURRENT ROW
    ) AS cantidad FROM base WHERE estado = 'RECHAZADO'
), entradas_diarias AS (
    SELECT b.*, ROW_NUMBER() OVER (
        PARTITION BY b.persona_id, b.fecha_hora::date ORDER BY b.fecha_hora, b.id
    ) AS posicion
    FROM base b WHERE b.tipo_evento = 'ENTRADA' AND b.estado = 'PERMITIDO'
), baseline AS (
    SELECT persona_id, AVG(minuto_dia) AS minuto_promedio, COUNT(*) AS dias
    FROM entradas_diarias WHERE posicion = 1 GROUP BY persona_id HAVING COUNT(*) >= 10
), anomalias AS (
    SELECT 'ACCESO_NOCTURNO'::varchar AS tipo, b.* FROM base b WHERE b.hora BETWEEN 0 AND 4
    UNION ALL SELECT 'ACCESO_TEMPRANO'::varchar, b.* FROM base b WHERE b.hora = 5
    UNION ALL SELECT 'ACCESO_TARDIO'::varchar, b.* FROM base b WHERE b.hora >= 22
    UNION ALL SELECT 'ACCESOS_REPETITIVOS'::varchar, b.* FROM base b JOIN conteo_5 c ON c.id=b.id WHERE c.cantidad >= 3
    UNION ALL SELECT 'RECHAZOS_REPETITIVOS'::varchar, b.* FROM base b JOIN rechazos_10 r ON r.id=b.id WHERE r.cantidad >= 3
    UNION ALL
    SELECT 'DESVIACION_HORARIO'::varchar, ed.id, ed.persona_id, ed.fecha_hora,
           ed.tipo_evento, ed.estado, ed.codigo_biometrico, ed.nombres, ed.apellidos, ed.hora, ed.minuto_dia
    FROM entradas_diarias ed JOIN baseline bl ON bl.persona_id=ed.persona_id
    WHERE ed.posicion=1 AND ABS(ed.minuto_dia-bl.minuto_promedio)>120
)
SELECT a.tipo, a.persona_id, a.codigo_biometrico, a.nombres, a.apellidos, a.id, a.fecha_hora
FROM anomalias a
WHERE (CAST(NULL AS varchar) IS NULL OR a.tipo=CAST(NULL AS varchar))
  AND (CAST(NULL AS bigint) IS NULL OR a.persona_id=CAST(NULL AS bigint))
ORDER BY a.fecha_hora DESC, a.id DESC, a.tipo LIMIT 20 OFFSET 0;

\echo 'ANOMALIAS_COUNT'
EXPLAIN (ANALYZE, BUFFERS, VERBOSE, FORMAT TEXT)
WITH base AS (
    SELECT ra.id, ra.persona_id, ra.fecha_hora, ra.tipo_evento, ra.estado,
           p.codigo_biometrico, p.nombres, p.apellidos,
           EXTRACT(HOUR FROM ra.fecha_hora)::integer AS hora,
           EXTRACT(EPOCH FROM ra.fecha_hora::time) / 60.0 AS minuto_dia
    FROM registros_acceso ra INNER JOIN personas p ON p.id = ra.persona_id
    WHERE ra.fecha_hora >= :'inicio'::timestamp AND ra.fecha_hora < :'fin'::timestamp
), conteo_5 AS (
    SELECT id, COUNT(*) OVER (
        PARTITION BY persona_id ORDER BY fecha_hora
        RANGE BETWEEN INTERVAL '5 minutes' PRECEDING AND CURRENT ROW
    ) AS cantidad FROM base
), rechazos_10 AS (
    SELECT id, COUNT(*) OVER (
        PARTITION BY persona_id ORDER BY fecha_hora
        RANGE BETWEEN INTERVAL '10 minutes' PRECEDING AND CURRENT ROW
    ) AS cantidad FROM base WHERE estado = 'RECHAZADO'
), entradas_diarias AS (
    SELECT b.*, ROW_NUMBER() OVER (
        PARTITION BY b.persona_id, b.fecha_hora::date ORDER BY b.fecha_hora, b.id
    ) AS posicion
    FROM base b WHERE b.tipo_evento = 'ENTRADA' AND b.estado = 'PERMITIDO'
), baseline AS (
    SELECT persona_id, AVG(minuto_dia) AS minuto_promedio, COUNT(*) AS dias
    FROM entradas_diarias WHERE posicion = 1 GROUP BY persona_id HAVING COUNT(*) >= 10
), anomalias AS (
    SELECT 'ACCESO_NOCTURNO'::varchar AS tipo, b.* FROM base b WHERE b.hora BETWEEN 0 AND 4
    UNION ALL SELECT 'ACCESO_TEMPRANO'::varchar, b.* FROM base b WHERE b.hora = 5
    UNION ALL SELECT 'ACCESO_TARDIO'::varchar, b.* FROM base b WHERE b.hora >= 22
    UNION ALL SELECT 'ACCESOS_REPETITIVOS'::varchar, b.* FROM base b JOIN conteo_5 c ON c.id=b.id WHERE c.cantidad >= 3
    UNION ALL SELECT 'RECHAZOS_REPETITIVOS'::varchar, b.* FROM base b JOIN rechazos_10 r ON r.id=b.id WHERE r.cantidad >= 3
    UNION ALL
    SELECT 'DESVIACION_HORARIO'::varchar, ed.id, ed.persona_id, ed.fecha_hora,
           ed.tipo_evento, ed.estado, ed.codigo_biometrico, ed.nombres, ed.apellidos, ed.hora, ed.minuto_dia
    FROM entradas_diarias ed JOIN baseline bl ON bl.persona_id=ed.persona_id
    WHERE ed.posicion=1 AND ABS(ed.minuto_dia-bl.minuto_promedio)>120
)
SELECT COUNT(*) FROM anomalias a
WHERE (CAST(NULL AS varchar) IS NULL OR a.tipo=CAST(NULL AS varchar))
  AND (CAST(NULL AS bigint) IS NULL OR a.persona_id=CAST(NULL AS bigint));

\echo 'ANOMALIAS_RESUMEN'
EXPLAIN (ANALYZE, BUFFERS, VERBOSE, FORMAT TEXT)
WITH base AS (
    SELECT ra.id, ra.persona_id, ra.fecha_hora, ra.tipo_evento, ra.estado,
           p.codigo_biometrico, p.nombres, p.apellidos,
           EXTRACT(HOUR FROM ra.fecha_hora)::integer AS hora,
           EXTRACT(EPOCH FROM ra.fecha_hora::time) / 60.0 AS minuto_dia
    FROM registros_acceso ra INNER JOIN personas p ON p.id = ra.persona_id
    WHERE ra.fecha_hora >= :'inicio'::timestamp AND ra.fecha_hora < :'fin'::timestamp
), conteo_5 AS (
    SELECT id, COUNT(*) OVER (
        PARTITION BY persona_id ORDER BY fecha_hora
        RANGE BETWEEN INTERVAL '5 minutes' PRECEDING AND CURRENT ROW
    ) AS cantidad FROM base
), rechazos_10 AS (
    SELECT id, COUNT(*) OVER (
        PARTITION BY persona_id ORDER BY fecha_hora
        RANGE BETWEEN INTERVAL '10 minutes' PRECEDING AND CURRENT ROW
    ) AS cantidad FROM base WHERE estado = 'RECHAZADO'
), entradas_diarias AS (
    SELECT b.*, ROW_NUMBER() OVER (
        PARTITION BY b.persona_id, b.fecha_hora::date ORDER BY b.fecha_hora, b.id
    ) AS posicion
    FROM base b WHERE b.tipo_evento = 'ENTRADA' AND b.estado = 'PERMITIDO'
), baseline AS (
    SELECT persona_id, AVG(minuto_dia) AS minuto_promedio, COUNT(*) AS dias
    FROM entradas_diarias WHERE posicion = 1 GROUP BY persona_id HAVING COUNT(*) >= 10
), anomalias AS (
    SELECT 'ACCESO_NOCTURNO'::varchar AS tipo, b.* FROM base b WHERE b.hora BETWEEN 0 AND 4
    UNION ALL SELECT 'ACCESO_TEMPRANO'::varchar, b.* FROM base b WHERE b.hora = 5
    UNION ALL SELECT 'ACCESO_TARDIO'::varchar, b.* FROM base b WHERE b.hora >= 22
    UNION ALL SELECT 'ACCESOS_REPETITIVOS'::varchar, b.* FROM base b JOIN conteo_5 c ON c.id=b.id WHERE c.cantidad >= 3
    UNION ALL SELECT 'RECHAZOS_REPETITIVOS'::varchar, b.* FROM base b JOIN rechazos_10 r ON r.id=b.id WHERE r.cantidad >= 3
    UNION ALL
    SELECT 'DESVIACION_HORARIO'::varchar, ed.id, ed.persona_id, ed.fecha_hora,
           ed.tipo_evento, ed.estado, ed.codigo_biometrico, ed.nombres, ed.apellidos, ed.hora, ed.minuto_dia
    FROM entradas_diarias ed JOIN baseline bl ON bl.persona_id=ed.persona_id
    WHERE ed.posicion=1 AND ABS(ed.minuto_dia-bl.minuto_promedio)>120
)
SELECT (SELECT COUNT(*) FROM base), COUNT(*),
       COUNT(*) FILTER (WHERE tipo='ACCESO_NOCTURNO'),
       COUNT(*) FILTER (WHERE tipo='ACCESO_TEMPRANO'),
       COUNT(*) FILTER (WHERE tipo='ACCESO_TARDIO'),
       COUNT(*) FILTER (WHERE tipo='ACCESOS_REPETITIVOS'),
       COUNT(*) FILTER (WHERE tipo='RECHAZOS_REPETITIVOS'),
       COUNT(*) FILTER (WHERE tipo='DESVIACION_HORARIO')
FROM anomalias;
