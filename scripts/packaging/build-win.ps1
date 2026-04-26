param(
    [string]$RuntimePath,
    [string]$Launch4jExe = "C:\Program Files (x86)\Launch4j\launch4jc.exe",
    [string]$InnoExe = "C:\Program Files (x86)\Inno Setup 6\ISCC.exe"
)

$ErrorActionPreference = "Stop"

$ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..").Path
$DistApp = Join-Path $ProjectRoot "dist\app"
$RuntimeDest = Join-Path $DistApp "runtime"
$JarPath = Join-Path $ProjectRoot "target\original-BookTech.jar"
$JarPathFallback = Join-Path $ProjectRoot "target\BookTech.jar"
$DependencyTarget = Join-Path $ProjectRoot "target\dependency"
$DbSourcePrimary = Join-Path $ProjectRoot "src\main\java\database\BookTechDB.db"
$DbSourceFallback = Join-Path $ProjectRoot "src\main\resources\database\BookTechDB.db"
$Launch4jConfig = Join-Path $ProjectRoot "scripts\packaging\launch4j\booktech.xml"
$InnoScript = Join-Path $ProjectRoot "scripts\packaging\inno\BookTech.iss"
$ExePath = Join-Path $DistApp "BookTech.exe"

if ([string]::IsNullOrWhiteSpace($RuntimePath)) {
    throw "Debes indicar -RuntimePath con la ruta de un JRE/JDK portable (ej: C:\runtime\jdk-21-jre)."
}

$RuntimePathResolved = (Resolve-Path $RuntimePath).Path

Write-Host "[1/6] Limpiando y compilando jar..."
Set-Location $ProjectRoot
.\mvnw.cmd -q -DskipTests clean package
.\mvnw.cmd -q -DskipTests dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory="$DependencyTarget"

if (-not (Test-Path $JarPath)) {
    $JarPath = $JarPathFallback
}

if (-not (Test-Path $JarPath)) {
    throw "No se encontro el jar esperado en target (original-BookTech.jar o BookTech.jar)."
}

Write-Host "[2/6] Preparando carpeta dist..."
if (Test-Path $DistApp) {
    try {
        Remove-Item $DistApp -Recurse -Force
    } catch {
        throw "No se pudo limpiar dist\\app. Cierra BookTech.exe (y cualquier javaw.exe del paquete) y vuelve a ejecutar el script."
    }
}
New-Item -ItemType Directory -Path $DistApp | Out-Null

Write-Host "[3/6] Copiando artefactos..."
Copy-Item $JarPath (Join-Path $DistApp "BookTech.jar") -Force
New-Item -ItemType Directory -Path (Join-Path $DistApp "database") | Out-Null
New-Item -ItemType Directory -Path (Join-Path $DistApp "lib") | Out-Null

$DbSourceToUse = $null
if (Test-Path $DbSourcePrimary) {
    $DbSourceToUse = $DbSourcePrimary
} elseif (Test-Path $DbSourceFallback) {
    $DbSourceToUse = $DbSourceFallback
}

if (-not $DbSourceToUse) {
    throw "No se encontro BookTechDB.db en src\\main\\java\\database ni en src\\main\\resources\\database."
}

Copy-Item $DbSourceToUse (Join-Path $DistApp "database\BookTechDB.db") -Force

$DependencyJars = Get-ChildItem -Path $DependencyTarget -Filter "*.jar" -ErrorAction SilentlyContinue
if (-not $DependencyJars) {
    throw "No se encontraron dependencias runtime en target\\dependency."
}
Copy-Item (Join-Path $DependencyTarget "*.jar") (Join-Path $DistApp "lib") -Force

Write-Host "[4/6] Copiando runtime embebido..."
Copy-Item $RuntimePathResolved $RuntimeDest -Recurse -Force

if (-not (Test-Path $Launch4jExe)) {
    throw "No se encontro Launch4j: $Launch4jExe"
}

Write-Host "[5/6] Generando EXE con Launch4j..."
& "$Launch4jExe" "$Launch4jConfig"
if ($LASTEXITCODE -ne 0 -or -not (Test-Path $ExePath)) {
    throw "Launch4j no genero BookTech.exe correctamente."
}

if (-not (Test-Path $InnoExe)) {
    throw "No se encontro Inno Setup (ISCC): $InnoExe"
}

Write-Host "[6/6] Generando instalador con Inno Setup..."
& "$InnoExe" "$InnoScript"

Write-Host "Listo. Revisa dist\app (exe) y dist\installer (setup)."





