# HTTP Latency Analysis SMALL — SQL logging ON/OFF

Prueba controlada realizada el 12 de agosto de 2026 sobre 10.006 registros y rango 2026-01-01 a 2026-08-12.

Cada escenario utilizó una JVM nueva, el mismo JAR instrumentado, el mismo puerto alternativo, la misma base de datos, una ejecución de warm-up y siete ejecuciones medidas por endpoint. La configuración se aplicó por línea de comandos; `application.properties` no fue modificado.

## Configuración de los escenarios

### Logging ON

```text
--spring.jpa.show-sql=true
--spring.jpa.properties.hibernate.format_sql=true
```

### Logging OFF

```text
--spring.jpa.show-sql=false
--spring.jpa.properties.hibernate.format_sql=false
```

Las dos instancias temporales fueron detenidas después de las pruebas. La instrumentación Java también fue retirada al finalizar.

## Descomposición promedio — logging ON

| Etapa | Anomalías resumen | Anomalías página |
|---|---:|---:|
| SQL contenido/resumen | 44.580 ms | 58.763 ms |
| SQL count | 0.000 ms | 56.793 ms |
| Mapping de 20 proyecciones | 0.000 ms | 1.908 ms |
| Construcción DTO/Page | 0.250 ms | 0.017 ms |
| Service total | 44.838 ms | 117.492 ms |
| Controller total | 46.599 ms | 119.367 ms |
| Post-controller: serialización/escritura | 1.929 ms | 3.536 ms |
| Backend total medido por filtro | 48.528 ms | 122.903 ms |
| Cliente/transporte local residual | 26.828 ms | 28.816 ms |
| HTTP total | 75.356 ms | 151.719 ms |

## Descomposición promedio — logging OFF

| Etapa | Anomalías resumen | Anomalías página |
|---|---:|---:|
| SQL contenido/resumen | 43.382 ms | 43.914 ms |
| SQL count | 0.000 ms | 42.987 ms |
| Mapping de 20 proyecciones | 0.000 ms | 1.336 ms |
| Construcción DTO/Page | 0.234 ms | 0.015 ms |
| Service total | 43.624 ms | 88.263 ms |
| Controller total | 45.445 ms | 90.030 ms |
| Post-controller: serialización/escritura | 2.005 ms | 2.943 ms |
| Backend total medido por filtro | 47.450 ms | 92.973 ms |
| Cliente/transporte local residual | 26.890 ms | 24.922 ms |
| HTTP total | 74.340 ms | 117.895 ms |

El tiempo post-controller se obtiene desde un filtro servlet que termina después de que Spring MVC escribe la respuesta. Incluye serialización Jackson, escritura HTTP y retorno por los filtros restantes; no es un benchmark aislado de Jackson. El residual cliente/transporte es `HTTP total − backend total` e incluye `Invoke-WebRequest`, loopback y procesamiento del cliente.

## Comparación ON/OFF

| Escenario | Anomalías resumen HTTP | Anomalías página HTTP |
|---|---:|---:|
| SQL logging ON | 75.356 ms | 151.719 ms |
| SQL logging OFF | 74.340 ms | 117.895 ms |
| Diferencia ON − OFF | 1.016 ms | 33.824 ms |

Logging SQL tuvo impacto medible en la página, que imprime dos consultas nativas extensas. No produjo segundos adicionales bajo salida redirigida a archivo. Para el resumen de una sola sentencia la diferencia fue aproximadamente un milisegundo.

## Sentencias SQL reales

La instrumentación registró:

- Resumen: exactamente 1 consulta SQL por request.
- Página: exactamente 2 consultas SQL por request, contenido y count.
- 16 requests contando warm-ups: 8 de resumen y 8 de página.
- Sentencias de anomalías encontradas en el log ON: `8 × 1 + 8 × 2 = 24`.
- No se registró logging de parámetros JDBC.

El archivo completo ON contiene 69 marcadores `Hibernate:` porque también incluye inicialización de la aplicación y autenticación. Solo 24 pertenecen a las 16 peticiones analíticas instrumentadas.

## N+1 y mapping

No existe N+1 en los endpoints analizados.

La consulta de contenido devuelve `AnomaliaProjection`. `aResponse` transforma cada una directamente a tipos simples y no accede a entidades JPA ni relaciones lazy. `personaRepository.findById()` se utiliza únicamente en el endpoint independiente de patrón por persona, no dentro del listado de anomalías.

Mapping medido:

- Logging ON: 1.908 ms para 20 filas.
- Logging OFF: 1.336 ms para 20 filas.

No se recalcula el patrón histórico por cada fila Java; el trabajo histórico forma parte de una sola consulta SQL.

## DTO y serialización

`AnomaliaResponse` contiene únicamente:

- enums;
- IDs `Long`;
- cadenas;
- `LocalDateTime`.

No contiene entidades ni colecciones JPA.

Payload medido:

| Endpoint | Elementos | Total lógico | Bytes HTTP |
|---|---:|---:|---:|
| Resumen | 1 DTO | 140 anomalías | 354 bytes |
| Página | 20 DTO | 140 anomalías | 5.651 bytes |

La serialización/escritura backend fue de aproximadamente 2–3,5 ms. No explica la discrepancia de segundos.

## SSE

SSE no interviene. `AccessEventStreamService` escucha únicamente `AccessCreatedEvent`, publicado después de crear un `RegistroAcceso`. Los GET analíticos no publican eventos ni recorren emisores SSE.

## Diagnóstico por categoría

### A. PostgreSQL

Es el componente dominante dentro del service estable, pero consume decenas de milisegundos en Java y alrededor de 100–250 ms bajo `EXPLAIN ANALYZE`, no decenas de segundos.

### B. Hibernate/JPA

No se encontró overhead independiente grande. Los tiempos de las llamadas al repositorio siguen de cerca el service total.

### C. N+1 queries

Descartado. Existe 1 sentencia para resumen y 2 para página.

### D. Mapping Java

Descartado como causa principal: 1,3–1,9 ms.

### E. Serialización JSON

Descartada como causa principal: aproximadamente 2–3,5 ms incluyendo escritura de respuesta.

### F. Logging/console

Impacto parcial demostrado: aproximadamente 30 ms adicionales en backend y 33,8 ms HTTP para la página. No reproduce por sí solo los 17–23 segundos cuando la salida está redirigida y consumida normalmente.

Una terminal que no drena stdout sí podría bloquear `show-sql`, pero esta prueba no demostró ese bloqueo; solo demuestra que logging tiene costo y que debe controlarse.

### G. Benchmark cliente

El cliente PowerShell añadió un residual estable cercano a 25–29 ms. No explica segundos.

### H. Otra causa demostrada

La causa específica de la ejecución antigua de 17–44 segundos no se reprodujo. Queda clasificada como outlier/condición transitoria del entorno original, no como latencia normal del endpoint. No debe usarse como baseline de optimización sin repetirlo bajo condiciones controladas.

## Causa principal

En condiciones controladas, la latencia estable se concentra en las consultas SQL y en la duplicación contenido + count. Logging SQL agrava la página, pero ninguna etapa produce segundos.

La discrepancia histórica HTTP vs PostgreSQL no puede atribuirse honestamente a una única capa con la evidencia actual. Los resultados descartan N+1, mapping, DTO, Jackson, payload y SSE. El baseline anterior estuvo afectado por una condición transitoria no reproducida, posiblemente relacionada con el entorno de ejecución o drenaje de salida, pero eso último no quedó demostrado directamente.

## Primera optimización posterior recomendada

Después de aceptar este diagnóstico, la primera optimización de código debería evitar ejecutar dos veces la detección para contenido y count. Es el único trabajo duplicado claramente demostrado y no requiere añadir índices ni cambiar la ingestión.

Antes de medir esa optimización debe crearse un nuevo baseline controlado con logging OFF, misma JVM/metodología y siete ejecuciones, preservando estos resultados como diagnóstico separado.
