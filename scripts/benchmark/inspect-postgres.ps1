[CmdletBinding()]
param(
    [ValidateSet('Inventory', 'Explain', 'ExplainAnomalies')]
    [string]$Mode = 'Inventory',
    [datetime]$From,
    [datetime]$To,
    [string]$Database = 'planprocons_erp',
    [string]$HostName = 'localhost',
    [int]$Port = 5432,
    [string]$Username = $env:DB_USERNAME,
    [string]$PsqlPath = 'C:\Program Files\PostgreSQL\17\bin\psql.exe',
    [string]$OutputDirectory = (Join-Path $PSScriptRoot 'results')
)

$ErrorActionPreference = 'Stop'
if (-not (Test-Path $PsqlPath)) { throw "No se encontró psql en $PsqlPath" }
if ([string]::IsNullOrWhiteSpace($Username) -or [string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    throw 'DB_USERNAME y DB_PASSWORD son obligatorios.'
}
if ($Mode -ne 'Inventory' -and (!$PSBoundParameters.ContainsKey('From') -or !$PSBoundParameters.ContainsKey('To'))) {
    throw 'From y To son obligatorios en modo Explain.'
}
if ($Mode -ne 'Inventory' -and $From.Date -gt $To.Date) { throw 'From no puede ser posterior a To.' }

New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
$env:PGPASSWORD = $env:DB_PASSWORD
$sqlFile = switch ($Mode) {
    'Inventory' { 'inventory.sql' }
    'Explain' { 'explain-analytics.sql' }
    'ExplainAnomalies' { 'explain-anomalies.sql' }
}
$outputFile = Join-Path $OutputDirectory (("{0}_{1}.txt" -f $Mode.ToLowerInvariant(), (Get-Date -Format 'yyyyMMdd_HHmmss')))
$arguments = @('-X', '-v', 'ON_ERROR_STOP=1', '-h', $HostName, '-p', $Port, '-U', $Username, '-d', $Database)
if ($Mode -ne 'Inventory') {
    $arguments += @('-v', "inicio=$($From.ToString('yyyy-MM-dd')) 00:00:00", '-v', "fin=$($To.Date.AddDays(1).ToString('yyyy-MM-dd')) 00:00:00")
}
$arguments += @('-f', (Join-Path $PSScriptRoot $sqlFile))
& $PsqlPath @arguments 2>&1 | Tee-Object -FilePath $outputFile
if ($LASTEXITCODE -ne 0) { throw "psql finalizó con código $LASTEXITCODE" }
Write-Host "Salida guardada en $outputFile" -ForegroundColor Green
