param(
    [string]$RuntimePath = "C:\Program Files\Java\jdk-21"
)

$ErrorActionPreference = "Stop"

# 1. Definir rutas
$ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..").Path
$BuildScript = Join-Path $PSScriptRoot "build-win.ps1"
$ReleaseFolder = Join-Path $ProjectRoot "dist\release_to_user"
$VersionProps = Join-Path $ProjectRoot "version.properties"

Write-Host "--- Iniciando Proceso de Lanzamiento (Release) ---" -ForegroundColor Cyan

# 2. Verificar version.properties
if (-not (Test-Path $VersionProps)) {
    throw "No se encontro version.properties en la raiz."
}

# 3. Ejecutar el build principal (esto sincroniza pom.xml y genera artefactos)
Write-Host "[1/3] Ejecutando empaquetado y sincronizacion..." -ForegroundColor Yellow
& "$BuildScript" -RuntimePath "$RuntimePath"

# 4. Preparar la carpeta de distribucion para el usuario
Write-Host "[2/3] Preparando carpeta para el usuario en dist\release_to_user..." -ForegroundColor Yellow

if (Test-Path $ReleaseFolder) {
    Remove-Item $ReleaseFolder -Recurse -Force
}

# Crear la estructura de carpetas exacta que necesita el sistema de updates
$UpdatesDest = Join-Path $ReleaseFolder "updates"
$StableDest = Join-Path $UpdatesDest "win-x64\stable"
New-Item -ItemType Directory -Path $StableDest -Force | Out-Null

# 5. Copiar los archivos necesarios
# El manifiesto generado por el build-win.ps1 (en la carpeta temporal de updates del repo)
$SourceManifest = Join-Path $ProjectRoot "updates\win-x64\stable\manifest.json"

if (-not (Test-Path $SourceManifest)) {
    throw "No se encontro el manifest.json generado."
}

# 6. Copiar instalador primero
$Props = ConvertFrom-StringData (Get-Content $VersionProps -Raw)
$CurrentVersion = $Props.'app.version'

$InstallerPattern = "BookTech-Setup-$CurrentVersion.exe"
Write-Host "Buscando instalador con patron: $InstallerPattern" -ForegroundColor Gray
$SourceInstallerFile = Get-ChildItem -Path (Join-Path $ProjectRoot "dist\installer") -Filter $InstallerPattern | Sort-Object LastWriteTime -Descending | Select-Object -First 1

if (-not $SourceInstallerFile) {
    Write-Host "ADVERTENCIA: No se encontro el instalador con version exacta. Buscando el mas reciente..." -ForegroundColor Yellow
    $SourceInstallerFile = Get-ChildItem -Path (Join-Path $ProjectRoot "dist\installer") -Filter "BookTech-Setup-*.exe" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
}

if (-not $SourceInstallerFile) {
    throw "No se encontro ningun instalador generado en dist\installer que coincida con BookTech-Setup-*.exe"
}

Write-Host "Copiando instalador: $($SourceInstallerFile.Name)" -ForegroundColor Gray
$FinalInstallerPath = Join-Path $UpdatesDest "BookTech-Setup.exe"
Copy-Item $SourceInstallerFile.FullName $FinalInstallerPath -Force

# 7. Recalcular el hash del archivo FINAL y actualizar el manifiesto que se le entrega al usuario
Write-Host "[3/3] Recalculando hash para asegurar coincidencia..." -ForegroundColor Yellow
$UpdateManifestScript = Join-Path $PSScriptRoot "update-manifest.ps1"
$Notes = $Props.'app.release.notes'
$MinSupported = $Props.'app.min.supported'
if (-not $MinSupported) { $MinSupported = "1.0.0" }

$ManifestDest = Join-Path $StableDest "manifest.json"

& "$UpdateManifestScript" `
    -Version "$CurrentVersion" `
    -ArtifactPath "$FinalInstallerPath" `
    -ManifestPath "$ManifestDest" `
    -Notes "$Notes" `
    -MinSupportedVersion "$MinSupported"

Write-Host "Proceso completado." -ForegroundColor Green
Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host "INSTRUCCIONES PARA ACTUALIZAR AL USUARIO:" -ForegroundColor Cyan
Write-Host "1. Toma la carpeta 'updates' que esta dentro de:"
Write-Host "   $ReleaseFolder"
Write-Host "2. Entregasela al usuario."
Write-Host "3. El usuario debe pegarla en: C:\BookTech"
Write-Host "   (Quedando asi: C:\BookTech\updates\BookTech-Setup.exe, etc.)"
Write-Host "==================================================================" -ForegroundColor Cyan
