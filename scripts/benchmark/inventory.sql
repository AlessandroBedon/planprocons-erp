\pset pager off
\timing on
SELECT version();
SELECT current_database() AS database_name, current_schema() AS schema_name;
SELECT COUNT(*) AS total_registros, MIN(fecha_hora) AS fecha_minima, MAX(fecha_hora) AS fecha_maxima
FROM registros_acceso;
SELECT pg_size_pretty(pg_total_relation_size('registros_acceso')) AS tamano_total,
       pg_size_pretty(pg_relation_size('registros_acceso')) AS tamano_tabla,
       pg_size_pretty(pg_indexes_size('registros_acceso')) AS tamano_indices;
SELECT indexname, indexdef
FROM pg_indexes
WHERE schemaname = current_schema() AND tablename = 'registros_acceso'
ORDER BY indexname;
