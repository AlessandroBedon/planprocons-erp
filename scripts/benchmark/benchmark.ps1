[CmdletBinding()]
param(
    [ValidateSet('baseline', 'optimized', 'medium', 'large')]
    [string]$Phase = 'baseline',
    [Parameter(Mandatory)]
    [int]$DatasetSize,
    [Parameter(Mandatory)]
    [datetime]$From,
    [Parameter(Mandatory)]
    [datetime]$To,
    [ValidateRange(3, 20)]
    [int]$Runs = 7,
    [ValidateRange(1, 5)]
    [int]$Warmups = 1,
    [string]$BaseUrl = 'http://localhost:8080',
    [string]$Username = $env:BENCHMARK_USERNAME,
    [string]$Password = $env:BENCHMARK_PASSWORD,
    [switch]$Append,
    [string]$ResultsDirectory = (Join-Path $PSScriptRoot 'results')
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$invariantCulture = [Globalization.CultureInfo]::InvariantCulture

if ($From.Date -gt $To.Date) { throw 'From no puede ser posterior a To.' }
if ([string]::IsNullOrWhiteSpace($Username) -or [string]::IsNullOrWhiteSpace($Password)) {
    throw 'Configure BENCHMARK_USERNAME y BENCHMARK_PASSWORD o use -Username y -Password.'
}

function Get-PercentileMedian([double[]]$Values) {
    $ordered = @($Values | Sort-Object)
    $middle = [math]::Floor($ordered.Count / 2)
    if ($ordered.Count % 2 -eq 1) { return $ordered[$middle] }
    return ($ordered[$middle - 1] + $ordered[$middle]) / 2.0
}

function Get-ContentHash([string]$Content) {
    $sha = [System.Security.Cryptography.SHA256]::Create()
    try {
        $parsed = $Content | ConvertFrom-Json
        $functionalContent = if ($null -ne $parsed.data) {
            $parsed.data | ConvertTo-Json -Depth 20 -Compress
        } else {
            $Content
        }
        $bytes = [Text.Encoding]::UTF8.GetBytes($functionalContent)
        return ([BitConverter]::ToString($sha.ComputeHash($bytes))).Replace('-', '').ToLowerInvariant()
    } finally { $sha.Dispose() }
}

$base = $BaseUrl.TrimEnd('/')
$loginBody = @{ username = $Username; password = $Password } | ConvertTo-Json
try {
    $login = Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType 'application/json' -Body $loginBody
} catch {
    throw "No fue posible autenticar el benchmark: $($_.Exception.Message)"
}
if ([string]::IsNullOrWhiteSpace([string]$login.token)) { throw 'Login válido, pero la respuesta no contiene token.' }

$headers = @{ Authorization = "Bearer $($login.token)"; Accept = 'application/json' }
$fromText = $From.ToString('yyyy-MM-dd')
$toText = $To.ToString('yyyy-MM-dd')
$dayText = $To.ToString('yyyy-MM-dd')
$endpoints = [ordered]@{
    resumen = "/api/dashboard/resumen?fecha=$dayText"
    accesos_por_hora = "/api/dashboard/accesos-por-hora?fecha=$dayText"
    accesos_por_dia = "/api/dashboard/accesos-por-dia?desde=$fromText&hasta=$toText"
    personas_frecuentes = "/api/dashboard/personas-frecuentes?desde=$fromText&hasta=$toText&limit=10"
    patrones = "/api/analisis/patrones?desde=$fromText&hasta=$toText"
    anomalias_resumen = "/api/analisis/anomalias/resumen?desde=$fromText&hasta=$toText"
    anomalias_pagina = "/api/analisis/anomalias?desde=$fromText&hasta=$toText&page=0&size=20"
}

New-Item -ItemType Directory -Force -Path $ResultsDirectory | Out-Null
$rawPath = Join-Path $ResultsDirectory "${Phase}_raw.csv"
$summaryPath = Join-Path $ResultsDirectory "${Phase}_summary.csv"
$environmentPath = Join-Path $ResultsDirectory "${Phase}_environment.json"
if ((Test-Path $rawPath) -and -not $Append) {
    throw "Ya existe $rawPath. Use -Append para conservarlo y añadir otro escenario."
}

$benchmarkId = [guid]::NewGuid().ToString()
$rows = [Collections.Generic.List[object]]::new()

foreach ($entry in $endpoints.GetEnumerator()) {
    $uri = "$base$($entry.Value)"
    Write-Host "Warm-up: $($entry.Key)" -ForegroundColor DarkGray
    for ($warmup = 1; $warmup -le $Warmups; $warmup++) {
        $null = Invoke-WebRequest -UseBasicParsing -Uri $uri -Headers $headers -TimeoutSec 180
    }

    for ($run = 1; $run -le $Runs; $run++) {
        $watch = [Diagnostics.Stopwatch]::StartNew()
        $status = 0
        $content = ''
        try {
            $response = Invoke-WebRequest -UseBasicParsing -Uri $uri -Headers $headers -TimeoutSec 180
            $status = [int]$response.StatusCode
            $content = [string]$response.Content
        } catch {
            $watch.Stop()
            if ($_.Exception.Response) { $status = [int]$_.Exception.Response.StatusCode }
            throw "Falló $($entry.Key), ejecución $run, HTTP ${status}: $($_.Exception.Message)"
        }
        $watch.Stop()
        $rows.Add([pscustomobject]@{
            benchmark_id = $benchmarkId
            measured_at_utc = [datetime]::UtcNow.ToString('o')
            phase = $Phase
            dataset_size = $DatasetSize
            desde = $fromText
            hasta = $toText
            endpoint = $entry.Key
            run = $run
            time_ms = $watch.Elapsed.TotalMilliseconds.ToString('F3', $invariantCulture)
            status = $status
            response_bytes = [Text.Encoding]::UTF8.GetByteCount($content)
            response_sha256 = Get-ContentHash $content
        })
        Write-Host ("{0,-22} run {1}: {2:N3} ms" -f $entry.Key, $run, $watch.Elapsed.TotalMilliseconds)
    }
}

if ($Append -and (Test-Path $rawPath)) {
    $rows | Export-Csv -Path $rawPath -NoTypeInformation -Encoding UTF8 -Append
} else {
    $rows | Export-Csv -Path $rawPath -NoTypeInformation -Encoding UTF8
}

$allRows = @(Import-Csv $rawPath)
$summary = $allRows | Group-Object benchmark_id, phase, dataset_size, desde, hasta, endpoint | ForEach-Object {
    $group = $_.Group
    $times = [double[]]@($group | ForEach-Object { [double]::Parse($_.time_ms, $invariantCulture) })
    [pscustomobject]@{
        benchmark_id = $group[0].benchmark_id
        phase = $group[0].phase
        dataset_size = [int]$group[0].dataset_size
        desde = $group[0].desde
        hasta = $group[0].hasta
        endpoint = $group[0].endpoint
        runs = $times.Count
        min_ms = ([double](($times | Measure-Object -Minimum).Minimum)).ToString('F3', $invariantCulture)
        max_ms = ([double](($times | Measure-Object -Maximum).Maximum)).ToString('F3', $invariantCulture)
        avg_ms = ([double](($times | Measure-Object -Average).Average)).ToString('F3', $invariantCulture)
        median_ms = ([double](Get-PercentileMedian $times)).ToString('F3', $invariantCulture)
        status = ($group.status | Sort-Object -Unique) -join '|'
        response_sha256 = ($group.response_sha256 | Sort-Object -Unique) -join '|'
    }
}
$summary | Sort-Object {[int]$_.dataset_size}, endpoint | Export-Csv -Path $summaryPath -NoTypeInformation -Encoding UTF8

$environment = [ordered]@{
    captured_at_utc = [datetime]::UtcNow.ToString('o')
    benchmark_id = $benchmarkId
    phase = $Phase
    dataset_size = $DatasetSize
    range = @{ desde = $fromText; hasta = $toText }
    warmups = $Warmups
    measured_runs = $Runs
    base_url = $base
    operating_system = [Environment]::OSVersion.VersionString
    architecture = [Runtime.InteropServices.RuntimeInformation]::OSArchitecture.ToString()
    processor_identifier = $env:PROCESSOR_IDENTIFIER
    logical_processors = [Environment]::ProcessorCount
    powershell = $PSVersionTable.PSVersion.ToString()
    note = 'Mediciones posteriores al warm-up; incluyen HTTP, serialización y red local. No se limpiaron caches de PostgreSQL ni del sistema operativo.'
}
$environment | ConvertTo-Json -Depth 4 | Set-Content -Path $environmentPath -Encoding UTF8

if ($Phase -eq 'optimized') {
    $baselinePath = Join-Path $ResultsDirectory 'baseline_summary.csv'
    if (Test-Path $baselinePath) {
        $baseline = @(Import-Csv $baselinePath)
        $optimized = @(Import-Csv $summaryPath)
        $comparison = foreach ($after in $optimized) {
            $before = $baseline | Where-Object {
                $_.dataset_size -eq $after.dataset_size -and $_.desde -eq $after.desde -and
                $_.hasta -eq $after.hasta -and $_.endpoint -eq $after.endpoint
            } | Select-Object -Last 1
            if ($before) {
                $beforeAverage = [double]::Parse($before.avg_ms, $invariantCulture)
                $afterAverage = [double]::Parse($after.avg_ms, $invariantCulture)
                [pscustomobject]@{
                    dataset_size = [int]$after.dataset_size
                    desde = $after.desde
                    hasta = $after.hasta
                    endpoint = $after.endpoint
                    baseline_avg_ms = $beforeAverage.ToString('F3', $invariantCulture)
                    optimized_avg_ms = $afterAverage.ToString('F3', $invariantCulture)
                    improvement_percent = if ($beforeAverage -eq 0) { '0.000' } else { ((($beforeAverage - $afterAverage) / $beforeAverage) * 100).ToString('F3', $invariantCulture) }
                }
            }
        }
        $comparison | Export-Csv (Join-Path $ResultsDirectory 'comparison.csv') -NoTypeInformation -Encoding UTF8
    }
}

Write-Host "Resultados: $rawPath" -ForegroundColor Green
Write-Host "Resumen:   $summaryPath" -ForegroundColor Green
