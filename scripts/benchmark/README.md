# Benchmark Big Data V1

Herramienta reproducible para medir la latencia HTTP real de los endpoints analíticos de PlanProcons. No genera datasets, no elimina registros, no crea índices y no modifica consultas de producción.

## Requisitos

- Backend disponible en `http://localhost:8080`.
- PostgreSQL disponible y dataset ya preparado.
- PowerShell 5.1 o superior.
- `BENCHMARK_USERNAME` y `BENCHMARK_PASSWORD` configuradas en la sesión.
- Para inspección SQL: `DB_USERNAME`, `DB_PASSWORD` y `psql` de PostgreSQL 17.

Las credenciales y el JWT nunca se escriben en resultados ni se muestran en consola.

## 1. Inventario antes del baseline

```powershell
cd "C:\Proyectos\PlanProcons ERP"
.\scripts\benchmark\inspect-postgres.ps1 -Mode Inventory
```

Registra versión PostgreSQL, cantidad y rango real, tamaño de tabla/índices e índices materializados. La salida queda en `scripts/benchmark/results/inventory_*.txt`.

## 2. Baseline SMALL

```powershell
$env:BENCHMARK_USERNAME = "usuario_desarrollo"
$env:BENCHMARK_PASSWORD = Read-Host "Password"

.\scripts\benchmark\benchmark.ps1 `
  -Phase baseline `
  -DatasetSize 10000 `
  -From "2026-01-01" `
  -To "2026-07-31" `
  -Warmups 1 `
  -Runs 7
```

`DatasetSize` debe ser la cantidad real presente, no una etiqueta aproximada. Use el inventario para obtenerla. `From` y `To` deben coincidir con el escenario documentado.

Para añadir MEDIUM y LARGE sin reemplazar el baseline existente:

```powershell
.\scripts\benchmark\benchmark.ps1 -Phase baseline -DatasetSize 100000 -From "2026-01-01" -To "2026-07-31" -Append
```

No ejecute 100k o 500k antes de revisar el resultado SMALL. La herramienta nunca crea esos registros automáticamente.

## Metodología

Cada endpoint recibe una ejecución de calentamiento no registrada y luego 7 ejecuciones medidas por defecto. Se usa `Stopwatch`, por lo que el tiempo incluye petición HTTP local, ejecución Spring/PostgreSQL, serialización y transferencia de la respuesta.

Se guardan mínimo, máximo, promedio y mediana. También se registra código HTTP, bytes y SHA-256 del campo `data` de cada respuesta para detectar cambios funcionales sin que el timestamp de `ApiResponse` produzca falsos cambios.

PostgreSQL y Windows conservan cachés. Estas son mediciones posteriores al warm-up, no pruebas de disco frío. No se limpian cachés del sistema operativo.

## Endpoints medidos

- `dashboard/resumen` y `accesos-por-hora`: usan el día indicado por `To`, respetando su contrato real.
- `accesos-por-dia`, `personas-frecuentes`, `patrones`, `anomalias/resumen`: usan `From`–`To`.
- `anomalias`: primera página, 20 elementos, más su conteo total ejecutado por el backend.

## Resultados

- `baseline_raw.csv`: una fila por ejecución.
- `baseline_summary.csv`: estadísticas agrupadas por ejecución, volumen, rango y endpoint.
- `baseline_environment.json`: entorno y metodología.
- Después de optimizar: `optimized_raw.csv`, `optimized_summary.csv` y `comparison.csv`.

No edite los CSV manualmente. `-Append` preserva escenarios anteriores; sin esa opción el script se detiene si el archivo ya existe para evitar perder mediciones.

Los valores decimales se escriben con punto mediante cultura invariante, independientemente de la configuración regional de Windows. Esto facilita su importación posterior en Python, R, Excel o herramientas de visualización.

## 3. EXPLAIN ANALYZE después del baseline

```powershell
.\scripts\benchmark\inspect-postgres.ps1 `
  -Mode Explain `
  -From "2026-01-01" `
  -To "2026-07-31"
```

La salida `explain_*.txt` incluye `ANALYZE`, `BUFFERS` y planes para resumen, agrupaciones temporales, personas frecuentes y patrones. Revise `Seq Scan`, `Index Scan`, `Bitmap Index Scan`, sorts, agregaciones, filas estimadas/reales, buffers y tiempo de ejecución.

Para las tres consultas exactas basadas en `RegistroAccesoRepository.ANOMALIAS_CTE`:

```powershell
.\scripts\benchmark\inspect-postgres.ps1 `
  -Mode ExplainAnomalies `
  -From "2026-01-01" `
  -To "2026-07-31"
```

Este modo separa contenido paginado, conteo y resumen para cuantificar cada ejecución completa de la CTE.

La consulta completa de anomalías está centralizada en `RegistroAccesoRepository.ANOMALIAS_CTE`; debido a su longitud y duplicación en listado/conteo/resumen, debe correlacionarse el SQL exacto mostrado por Hibernate con los tiempos del endpoint antes de proponer cambios.

## 4. Medición posterior

Solo después de aprobar e implementar una optimización justificada:

```powershell
.\scripts\benchmark\benchmark.ps1 `
  -Phase optimized `
  -DatasetSize 10000 `
  -From "2026-01-01" `
  -To "2026-07-31" `
  -Runs 7
```

Use exactamente volumen, rango, warm-ups y repeticiones del baseline. `comparison.csv` calcula `((baseline_avg - optimized_avg) / baseline_avg) * 100`. Una cifra negativa representa una regresión.

## Validez funcional

Compare `response_sha256` antes/después únicamente cuando el dataset no haya cambiado. Si hubo nuevas marcaciones, valide los valores de negocio manualmente porque un hash diferente puede ser legítimo. Una optimización no es válida si cambia totales, picos, personas frecuentes o anomalías.

## Limitaciones V1

- Mide latencia secuencial, no concurrencia.
- No separa tiempo Java, red y PostgreSQL; `EXPLAIN ANALYZE` aporta el componente SQL.
- No mide disco frío.
- El volumen total puede incluir filas fuera del rango consultado; ambos valores se documentan por separado.
- La ingesta batch queda como medición independiente futura.
