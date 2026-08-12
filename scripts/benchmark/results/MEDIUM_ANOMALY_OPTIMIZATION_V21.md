# Optimización V2.1 — `work_mem` local para anomalías

## Implementación

Las transacciones de:

- `GET /api/analisis/anomalias`;
- `GET /api/analisis/anomalias/resumen`;

ejecutan antes de la consulta analítica un único método de preparación:

```sql
SELECT set_config('enable_nestloop', 'off', true)
       || ':' || set_config('work_mem', '16MB', true)
```

El tercer argumento `true` da alcance local a la transacción, equivalente a `SET LOCAL`. Se conserva el control de nested loops de V1 y se añade 16 MB exclusivamente para estas dos transacciones. No se modificaron `postgresql.conf`, `application.properties`, roles de PostgreSQL ni el valor global.

Ambos servicios ya usan `@Transactional(readOnly = true)`, por lo que la preparación y consulta utilizan la misma transacción/conexión. Al hacer commit o rollback, PostgreSQL restaura automáticamente los valores antes de que Hikari reutilice la conexión.

## Verificación de alcance

Prueba explícita:

| Momento | work_mem | enable_nestloop |
|---|---:|---:|
| Antes | 4 MB | on |
| Durante la transacción | 16 MB | off |
| Después del commit | 4 MB | on |

La evidencia está en `work_mem_scope_v21.txt`.

## Regresión funcional

Los seis SHA-256 coinciden exactamente con V2:

| Caso | SHA-256 | Coincide |
|---|---|---:|
| página 0 | `ec77de2e55fcd48fc8987417fdeea836d769a906d4a1535e6c318c2162bc2494` | Sí |
| página 1 | `237ab4d6445315ab79b41db85a971510338f574304a9233b35ad49f8b53ee8eb` | Sí |
| fuera de rango | `23d0eb3979da51c5de9ad3a8eee9f58572c250c82588e7d4a1653ee4615a0b05` | Sí |
| filtro tipo | `163de70a4ea09d0d13263b166e121923575c38ae7122c1155bf9a993d2477d25` | Sí |
| filtro persona | `0cc9713d82385f4593fc680cfa0ef497822308a402c48ba91f55d971d7978d1a` | Sí |
| período vacío | `04e6143ddb2920cc7171ffdaf329e03464c8f817f39a8cf72708099a67dc3121` | Sí |

También se conservaron contenido, orden, filtros y totales: 1.520 general, 200 por tipo, 5 por persona y 0 en el período vacío.

## Benchmark HTTP

Condiciones: 100.000 registros, `2026-01-01`–`2026-08-12`, logging SQL OFF, una llamada de calentamiento y siete ejecuciones secuenciales.

| Métrica | V2 4 MB | V2.1 16 MB | Diferencia | Mejora |
|---|---:|---:|---:|---:|
| Resumen HTTP promedio | 990,592 ms | 741,393 ms | -249,199 ms | 25,157 % |
| Página HTTP promedio | 865,874 ms | 653,568 ms | -212,306 ms | 24,519 % |
| SQL Execution Time | 900,693 ms | 458,787 ms | -441,906 ms | 49,063 % |
| Temp read | 5.570 | 0 | -5.570 | 100 % |
| Temp written | 2.673 | 0 | -2.673 | 100 % |

Distribución V2.1:

| Endpoint | Mínimo | Mediana | Máximo | HTTP | Total |
|---|---:|---:|---:|---:|---:|
| resumen | 657,821 ms | 754,750 ms | 810,394 ms | 200 | 1.520 |
| página | 626,826 ms | 654,160 ms | 689,490 ms | 200 | 1.520 |

No hubo respuestas HTTP erróneas ni picos equivalentes al problema previo de 15–17 segundos.

## EXPLAIN ANALYZE

- Execution Time: 458,787 ms.
- Sorts grandes: `quicksort`, 5.580 kB y 6.979 kB en memoria.
- Sort de rechazos: `quicksort`, 186 kB.
- Página: `top-N heapsort`, 29 kB.
- Orden final: `quicksort`, 27 kB.
- Temp blocks read: 0.
- Temp blocks written: 0.
- No aparece `external merge` ni uso de disco para sort.

## Costo y beneficio

`work_mem=16MB` no limita toda la petición a 16 MB. PostgreSQL puede asignar hasta ese límite a varias operaciones de sort/hash dentro de una consulta, y varias sesiones concurrentes pueden hacerlo simultáneamente. El alcance local reduce el impacto sobre el resto del ERP, pero una prueba local secuencial no sustituye una evaluación de concurrencia y memoria total.

V2.1 queda validada: build correcto, hashes idénticos, valores restaurados, endpoints funcionales y ausencia de cambios globales. Conforme al checkpoint definido, el sistema está listo para ejecutar posteriormente el benchmark LARGE de 500.000 registros. Esta fase no generó esos registros.
