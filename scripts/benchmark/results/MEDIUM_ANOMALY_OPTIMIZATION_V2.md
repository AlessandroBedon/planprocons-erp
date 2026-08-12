# Optimización V2 — anomalías sobre 100.000 registros

## Alcance

V2 modifica únicamente la consulta de anomalías. No se añadieron índices, vistas, tablas, cachés ni configuración permanente; tampoco se modificaron seguridad, frontend o SSE. El dataset permanece en exactamente 100.000 registros.

Baseline oficial conservado:

- resumen de anomalías: 1.146,455 ms HTTP;
- página de anomalías: 1.026,629 ms HTTP;
- SQL de página bajo `EXPLAIN`: 3.226,940 ms;
- temporales: 10.591 bloques leídos y 3.385 escritos.

## Ancho de la base analítica

Antes, `base` transportaba diez columnas:

1. `id`;
2. `persona_id`;
3. `fecha_hora`;
4. `tipo_evento`;
5. `estado`;
6. `codigo_biometrico`;
7. `nombres`;
8. `apellidos`;
9. `hora`;
10. `minuto_dia`.

Después transporta siete:

1. `id`;
2. `persona_id`;
3. `fecha_hora`;
4. `tipo_evento`;
5. `estado`;
6. `hora`;
7. `minuto_dia`.

`pg_column_size` sobre las 100.000 filas estimó 117,63 bytes/fila antes y 82,97 después: reducción aproximada de 29,5 %. Las ventanas, CTE y sorts ya no transportan código biométrico, nombres ni apellidos.

## Recorridos y joins

Las tres categorías horarias eran tres ramas independientes sobre `base`. Ahora una única rama usa `CASE`, conservando rangos exactamente disjuntos:

- horas 0–4: `ACCESO_NOCTURNO`;
- hora 5: `ACCESO_TEMPRANO`;
- hora 22 o posterior: `ACCESO_TARDIO`.

Los recorridos horarios bajaron de tres a uno. Contando las lecturas lógicas directas necesarias por ramas/ventanas y sus cruces, `base` baja conceptualmente de ocho recorridos a seis. Las anomalías repetitivas, rechazos y desviación conservan sus ventanas y thresholds originales.

El join con Persona se movió desde la construcción de las 100.000 filas de `base` al resultado paginado final. En el plan V2 aparece después de producir las 21 filas de página/metadatos. El resumen no necesita ese join.

Se conservaron `COUNT(*) OVER()`, fila técnica para páginas vacías, orden, filtros y control local del planificador de V1.

## Regresión funcional

Se comparó SHA-256 del objeto `data` antes y después de V2:

| Caso | SHA-256 | Coincide |
|---|---|---:|
| página 0 | `ec77de2e55fcd48fc8987417fdeea836d769a906d4a1535e6c318c2162bc2494` | Sí |
| página 1 | `237ab4d6445315ab79b41db85a971510338f574304a9233b35ad49f8b53ee8eb` | Sí |
| fuera de rango | `23d0eb3979da51c5de9ad3a8eee9f58572c250c82588e7d4a1653ee4615a0b05` | Sí |
| filtro tipo | `163de70a4ea09d0d13263b166e121923575c38ae7122c1155bf9a993d2477d25` | Sí |
| filtro persona | `0cc9713d82385f4593fc680cfa0ef497822308a402c48ba91f55d971d7978d1a` | Sí |
| período vacío | `04e6143ddb2920cc7171ffdaf329e03464c8f817f39a8cf72708099a67dc3121` | Sí |

También coinciden `totalElements`: 1.520 general, 200 para `ACCESO_TARDIO`, 5 para persona 56 y 0 para período vacío.

## Resultados oficiales V2

El benchmark HTTP mantuvo 100.000 registros, rango `2026-01-01`–`2026-08-12`, logging OFF, un warm-up y siete ejecuciones secuenciales.

| Métrica | Antes V2 | Después V2 | Mejora |
|---|---:|---:|---:|
| Anomalías resumen HTTP promedio | 1.146,455 ms | 990,592 ms | 13,595 % |
| Anomalías página HTTP promedio | 1.026,629 ms | 865,874 ms | 15,659 % |
| SQL página, `EXPLAIN` | 3.226,940 ms | 900,693 ms | 72,088 % |
| Temp blocks read | 10.591 | 5.570 | 47,408 % |
| Temp blocks written | 3.385 | 2.673 | 21,034 % |

Distribución HTTP posterior:

| Endpoint | Mínimo | Mediana | Máximo | HTTP |
|---|---:|---:|---:|---:|
| resumen | 842,576 ms | 904,561 ms | 1.338,143 ms | 200 |
| página | 826,578 ms | 862,773 ms | 931,549 ms | 200 |

El SQL mejoró más que HTTP porque `EXPLAIN VERBOSE/BUFFERS`, calentamiento, JDBC, autenticación y serialización no son mediciones equivalentes. Se presentan ambas capas sin sustituir una por otra.

En V2 con 4 MB todavía existen dos sorts externos: 3.472 kB y 3.336 kB. El ancho de `entradas_diarias` bajó de 742 a 188 bytes estimados por el plan, y el resultado intermedio de anomalías de 762 a 208 bytes.

## Experimento separado de `work_mem`

El valor real actual es 4 MB. Cada prueba utilizó `SET LOCAL` dentro de una transacción y la consulta V2; ningún valor quedó aplicado al backend o servidor.

| work_mem | Execution Time | Sort principal | Temp read | Temp written |
|---|---:|---|---:|---:|
| 4 MB | 900,693 ms | external merge, 3.472/3.336 kB disco | 5.570 | 2.673 |
| 8 MB | 756,897 ms | quicksort, 5.580/6.979 kB memoria | 4.210 | 842 |
| 16 MB | 434,756 ms | quicksort, 5.580/6.979 kB memoria | 0 | 0 |
| 32 MB | 421,168 ms | quicksort, 5.580/6.979 kB memoria | 0 | 0 |

Los external sorts desaparecen desde 8 MB, pero todavía hubo temporales de materialización del CTE. Desde 16 MB no se registraron bloques temporales. La diferencia entre 16 y 32 MB fue solo 13,588 ms en esta corrida, por lo que 16 MB es el candidato más prudente para una prueba de configuración posterior; no se recomienda aplicarlo globalmente aún.

`work_mem` se asigna por operación de sort/hash y potencialmente por sesión concurrente. Esta prueba local y secuencial no demuestra que aumentar memoria sea seguro bajo concurrencia ni que “más siempre sea mejor”.

## Evaluación

V2 es una mejora real y funcionalmente equivalente, pero la página sigue promediando 865,874 ms con 100.000 filas. No es suficiente para recomendar directamente un benchmark oficial de 500.000 registros: la consulta todavía recorre la base varias veces y realiza ventanas sobre todo el período.

La estrategia V3 recomendada es evaluar una representación incremental/persistida de anomalías o alertas durante la ingestión, con reconstrucción histórica controlada y reglas versionadas. Antes de eso puede hacerse una prueba aislada de `work_mem=16MB` por transacción para cuantificar el beneficio HTTP bajo el mismo benchmark, sin convertirlo todavía en configuración global.
