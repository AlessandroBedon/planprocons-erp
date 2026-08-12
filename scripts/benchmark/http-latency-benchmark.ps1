[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [ValidateSet('baseline_logging_on', 'baseline_logging_off', 'optimized_page_count', 'medium_optimized_v2', 'medium_optimized_v21')]
    [string]$Scenario,
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
    [string]$ResultsDirectory = (Join-Path $PSScriptRoot 'results')
)

$ErrorActionPreference = 'Stop'
$invariant = [Globalization.CultureInfo]::InvariantCulture
if ($From.Date -gt $To.Date) { throw 'From no puede ser posterior a To.' }
if ([string]::IsNullOrWhiteSpace($Username) -or [string]::IsNullOrWhiteSpace($Password)) {
    throw 'Configure BENCHMARK_USERNAME y BENCHMARK_PASSWORD.'
}

$base = $BaseUrl.TrimEnd('/')
$login = Invoke-RestMethod -Method Post -Uri "$base/auth/login" -ContentType 'application/json' `
    -Body (@{ username = $Username; password = $Password } | ConvertTo-Json)
if ([string]::IsNullOrWhiteSpace([string]$login.token)) { throw 'La autenticación no devolvió JWT.' }
$headers = @{ Authorization = "Bearer $($login.token)"; Accept = 'application/json' }
$desde = $From.ToString('yyyy-MM-dd')
$hasta = $To.ToString('yyyy-MM-dd')
$endpoints = [ordered]@{
    anomalias_resumen = "/api/analisis/anomalias/resumen?desde=$desde&hasta=$hasta"
    anomalias_pagina = "/api/analisis/anomalias?desde=$desde&hasta=$hasta&page=0&size=20"
}

New-Item -ItemType Directory -Force -Path $ResultsDirectory | Out-Null
$output = Join-Path $ResultsDirectory "${Scenario}_raw.csv"
if (Test-Path $output) { throw "Ya existe $output" }
$rows = [Collections.Generic.List[object]]::new()

foreach ($entry in $endpoints.GetEnumerator()) {
    $uri = "$base$($entry.Value)"
    for ($warmup = 1; $warmup -le $Warmups; $warmup++) {
        $null = Invoke-WebRequest -UseBasicParsing -Uri $uri -Headers $headers -TimeoutSec 180
    }
    for ($run = 1; $run -le $Runs; $run++) {
        $watch = [Diagnostics.Stopwatch]::StartNew()
        $response = Invoke-WebRequest -UseBasicParsing -Uri $uri -Headers $headers -TimeoutSec 180
        $watch.Stop()
        $json = $response.Content | ConvertFrom-Json
        $items = if ($entry.Key -eq 'anomalias_pagina') { @($json.data.content).Count } else { 1 }
        $total = if ($entry.Key -eq 'anomalias_pagina') { [long]$json.data.totalElements } else { [long]$json.data.totalAnomalias }
        $rows.Add([pscustomobject]@{
            scenario = $Scenario
            endpoint = $entry.Key
            run = $run
            http_ms = $watch.Elapsed.TotalMilliseconds.ToString('F3', $invariant)
            status = [int]$response.StatusCode
            payload_bytes = [Text.Encoding]::UTF8.GetByteCount([string]$response.Content)
            items = $items
            total_elements = $total
        })
    }
}
$rows | Export-Csv $output -NoTypeInformation -Encoding UTF8
Write-Host "Resultados guardados en $output" -ForegroundColor Green
