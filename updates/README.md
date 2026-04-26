# Actualizaciones (MVP seguro)

BookTech consulta un manifiesto JSON antes de iniciar la app.

## 1) Activar en un equipo

Crear archivo local:

`%LOCALAPPDATA%\\BookTech\\config\\updates.properties`

Contenido de ejemplo:

```properties
updates.enabled=true
updates.manifest.uri=file:///C:/BookTech/updates/win-x64/stable/manifest.json
```

## 2) Formato del manifiesto

Archivo: `updates/win-x64/stable/manifest.json`

```json
{
  "channel": "stable",
  "publishedAt": "2026-04-26T10:40:00-05:00",
  "latestVersion": "1.0.1",
  "minSupportedVersion": "1.0.0",
  "artifactPath": "C:/BookTech/updates/BookTech-Setup.exe",
  "sha256": "REEMPLAZAR_CON_HASH_REAL",
  "notes": "Correcciones menores y mejoras de estabilidad."
}
```

- `channel`: canal de actualización (`stable`, `beta`, etc.).
- `publishedAt`: fecha/hora de publicación del paquete.
- `latestVersion`: versión objetivo.
- `minSupportedVersion`: versión mínima recomendada para actualizar.
- `artifactPath`: ruta local o URL del instalador/paquete.
- `sha256`: hash SHA-256 esperado del paquete (obligatorio por seguridad).
- `notes`: texto mostrado al usuario.

## 3) Política de actualización

- No se usan actualizaciones obligatorias.
- El usuario decide actualizar desde el botón `Buscar actualizaciones`.
- Si el hash no coincide, la app bloquea la apertura del instalador.

## 4) Generar/actualizar manifiesto automáticamente

Script recomendado:

`scripts/packaging/update-manifest.ps1`

Ejemplo:

```powershell
Set-Location "C:\Users\sebas\Documents\GitHub\BookTech"
.\scripts\packaging\update-manifest.ps1 -Version "1.0.1" -ArtifactPath "C:\BookTech\updates\BookTech-Setup.exe" -Notes "Correcciones menores y mejoras de estabilidad."
```

Tambien puedes dejarlo automatico al empaquetar con `build-win.ps1`, que al finalizar genera/actualiza el manifest usando el setup recien creado.




