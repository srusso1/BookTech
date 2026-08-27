param(
    [string]$RuntimePath,
    [string]$Launch4jExe = "C:\Program Files (x86)\Launch4j\launch4jc.exe",
    [string]$InnoExe = "C:\Program Files (x86)\Inno Setup 6\ISCC.exe",
    [string]$Version,
    [string]$Notes = "Actualizacion generada por build-win.ps1",
    [string]$ManifestPath,
    [string]$MinSupportedVersion = "1.0.0",
    [string]$Channel = "stable",
    [string]$PublishUpdatesDir = "C:\BookTech\updates"
)

$ErrorActionPreference = "Stop"

$ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..").Path
$VersionPropsPath = Join-Path $ProjectRoot "version.properties"

# Cargar valores desde version.properties si existe
if (Test-Path $VersionPropsPath) {
    Write-Host "[info] Cargando configuracion desde version.properties..."
    $props = ConvertFrom-StringData (Get-Content $VersionPropsPath -Raw)
    
    $propVersion = $props.'app.version'
    if ($propVersion) {
        $Version = $propVersion
        
        # Sincronizar pom.xml con la version del properties
        $PomPath = Join-Path $ProjectRoot "pom.xml"
        if (Test-Path $PomPath) {
            [xml]$xml = Get-Content $PomPath
            if ($xml.project.version -ne $Version) {
                Write-Host "[info] Sincronizando pom.xml con la nueva version: $Version"
                $xml.project.version = $Version
                $xml.Save($PomPath)
            }
        }
    }

    if ($props.'app.release.notes' -and ($Notes -eq "Actualizacion generada por build-win.ps1")) { $Notes = $props.'app.release.notes' }
    if ($props.'app.min.supported' -and ($MinSupportedVersion -eq "1.0.0")) { $MinSupportedVersion = $props.'app.min.supported' }
}

$DistApp = Join-Path $ProjectRoot "dist\app"
$RuntimeDest = Join-Path $DistApp "runtime"
$JarPath = Join-Path $ProjectRoot "target\original-BookTech.jar"
$JarPathFallback = Join-Path $ProjectRoot "target\BookTech.jar"
$JarPathVersioned = $null
$DependencyTarget = Join-Path $ProjectRoot "target\dependency"
$DbSourcePrimary = Join-Path $ProjectRoot "src\main\java\database\BookTechDB.db"
$DbSourceFallback = Join-Path $ProjectRoot "src\main\resources\database\BookTechDB.db"
$Launch4jConfig = Join-Path $ProjectRoot "scripts\packaging\launch4j\booktech.xml"
$InnoScript = Join-Path $ProjectRoot "scripts\packaging\inno\BookTech.iss"
$UpdateManifestScript = Join-Path $ProjectRoot "scripts\packaging\update-manifest.ps1"
$DistInstaller = Join-Path $ProjectRoot "dist\installer"
$ExePath = Join-Path $DistApp "BookTech.exe"
$PublishedInstallerPath = Join-Path $PublishUpdatesDir "BookTech-Setup.exe"

function Get-EffectiveVersion {
    param(
        [string]$RequestedVersion,
        [string]$PomFile
    )

    if (-not [string]::IsNullOrWhiteSpace($RequestedVersion)) {
        return $RequestedVersion
    }

    if (-not (Test-Path $PomFile)) {
        throw "No se encontro pom.xml para inferir version. Usa -Version."
    }

    [xml]$pomXml = Get-Content $PomFile
    if ($pomXml.project.version -and -not [string]::IsNullOrWhiteSpace($pomXml.project.version)) {
        return $pomXml.project.version.Trim()
    }

    throw "No se pudo inferir la version desde pom.xml. Usa -Version."
}

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
    $JarPathVersioned = Get-ChildItem -Path (Join-Path $ProjectRoot "target") -Filter "BookTech-*.jar" -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if ($JarPathVersioned) {
        $JarPath = $JarPathVersioned.FullName
    }
}

if (-not (Test-Path $JarPath)) {
    throw "No se encontro el jar esperado en target (original-BookTech.jar, BookTech.jar o BookTech-<version>.jar)."
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
    $innoCandidates = @(
        "$env:LOCALAPPDATA\Programs\Inno Setup 6\ISCC.exe",
        "C:\Program Files\Inno Setup 6\ISCC.exe",
        "C:\Program Files (x86)\Inno Setup 6\ISCC.exe"
    )
    foreach ($cand in $innoCandidates) {
        if (Test-Path $cand) {
            $InnoExe = $cand
            break
        }
    }
}
if (-not (Test-Path $InnoExe)) {
    throw "No se encontro Inno Setup (ISCC): $InnoExe"
}

$EffectiveVersion = Get-EffectiveVersion -RequestedVersion $Version -PomFile (Join-Path $ProjectRoot "pom.xml")

Write-Host "[6/6] Generando instalador con Inno Setup..."
& "$InnoExe" "/dAppVersion=$EffectiveVersion" "$InnoScript"

if ($LASTEXITCODE -ne 0) {
    throw "Inno Setup no genero el instalador correctamente."
}

if (-not (Test-Path $UpdateManifestScript)) {
    throw "No se encontro el script de manifest: $UpdateManifestScript"
}

$InstallerArtifact = Get-ChildItem -Path $DistInstaller -Filter "BookTech-Setup*.exe" -File -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $InstallerArtifact) {
    throw "No se encontro el instalador generado en dist\\installer."
}

# $EffectiveVersion ya fue calculada antes de llamar a Inno Setup

if (-not (Test-Path $PublishUpdatesDir)) {
    New-Item -ItemType Directory -Path $PublishUpdatesDir -Force | Out-Null
}

Copy-Item $InstallerArtifact.FullName $PublishedInstallerPath -Force

if ([string]::IsNullOrWhiteSpace($ManifestPath)) {
    $ManifestPath = Join-Path $PublishUpdatesDir "win-x64\$Channel\manifest.json"
}

Write-Host "[post] Actualizando manifiesto de updates..."

$ManifestParams = @{
    Version = $EffectiveVersion
    ArtifactPath = $PublishedInstallerPath
    Channel = $Channel
    MinSupportedVersion = $MinSupportedVersion
    Notes = $Notes
    ManifestPath = $ManifestPath
}

& "$UpdateManifestScript" @ManifestParams

Write-Host "Listo. Revisa dist\app (exe), dist\installer (setup) y $PublishUpdatesDir (archivos de actualizacion)."




