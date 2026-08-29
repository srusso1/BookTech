# Actualizaciones de BookTech

BookTech cuenta con un sistema de actualizaciones modular, seguro y no destructivo que verifica la integridad criptográfica de cada versión antes de aplicarla.

---

## 1. Instrucciones de Entrega al Cliente

Cuando compiles una nueva versión con `prepare-release.ps1`, disponés de **dos métodos de entrega** según la preferencia de tu cliente:

### Opción A — Actualización Automática Asistida (Recomendada)
1. Toma la carpeta **`updates`** generada en `dist/release_to_user/updates`.
2. Entrégasela al usuario (vía USB, red local, ZIP o descarga).
3. El usuario debe pegar esa carpeta dentro del directorio de instalación:
   `C:\BookTech\`
   *(Quedando así: `C:\BookTech\updates\BookTech-Setup.exe` y `C:\BookTech\updates\win-x64\stable\manifest.json`)*.
4. Al abrir BookTech (o hacer clic en el botón *"Buscar actualizaciones"* en Rectoría - Ayuda), la aplicación detectará automáticamente la nueva versión, mostrará las notas de parche y validará el hash SHA-256 antes de lanzar el instalador.

### Opción B — Instalación Directa del Ejecutable
1. Toma el instalador independiente generado en `dist/installer/BookTech-Setup-<version>.exe`.
2. Entrégaselo al usuario y pídele que lo ejecute.
3. El instalador actualizará los ejecutables y librerías en `C:\BookTech` sin tocar sus datos existentes.

---

## 2. Garantía de Integridad y Protección de Datos

> [!IMPORTANT]
> **La base de datos del cliente nunca se borra ni se sobreescribe durante las actualizaciones.**

1. **Ubicación de la Base de Datos**: Los datos reales del cliente residen en `%LOCALAPPDATA%\BookTech\data\BookTechDB.db`, completamente aislados de los ejecutables del programa.
2. **Protección en el Instalador (`Inno Setup`)**: La directiva `onlyifdoesntexist` evita que el instalador sobrescriba una base de datos existente, y `uninsneveruninstall` asegura que nunca se borre si se desinstala el programa.
3. **Protección en Código Java (`ConexionSQLite.java`)**: `inicializarDbSiNoExiste()` detecta si el archivo ya existe y se conecta directamente a él sin tocar los datos.
4. **Auto-migración de Contraseñas (`BCrypt`)**: Al iniciar sesión tras actualizar, las contraseñas previas en texto plano se migran automáticamente a hashes seguros `$2a$` de forma transparente.

---

## 3. Configuración del Manifiesto (`updates.properties`)

BookTech consulta las actualizaciones según lo configurado en:
`src/main/resources/updates/updates.properties` (o override local por equipo en `%LOCALAPPDATA%\BookTech\config\updates.properties`):

```properties
updates.enabled=true
updates.manifest.uri=file:///C:/BookTech/updates/win-x64/stable/manifest.json
```

---

## 4. Formato del Manifiesto (`manifest.json`)

Ubicación: `updates/win-x64/stable/manifest.json`

```json
{
  "channel": "stable",
  "publishedAt": "2026-08-28T08:44:20-05:00",
  "latestVersion": "1.1.0",
  "minSupportedVersion": "1.0.0",
  "artifactPath": "C:\\BookTech\\updates\\BookTech-Setup.exe",
  "sha256": "3b8d4c4ca5ea36d39401055a99602133f14d26630019caaf1523a7ba5c132962",
  "notes": "Novedades de BookTech 1.1.0:\n- Módulo de Exportación a Excel (.xlsx) institucional con KPIs.\n- Gestión y CRUD completo de Docentes en el panel de Configuración.\n- Centro de Notificaciones y Alertas de vencimiento de préstamos.\n- Rediseño ergonómico Maestro-Detalle y mejoras de adaptabilidad visual.\n- Blindaje de contraseñas con hashing seguro BCrypt y optimizaciones SQL."
}
```

* `latestVersion`: Versión objetivo publicada.
* `minSupportedVersion`: Versión mínima recomendada.
* `artifactPath`: Ruta local o URL del instalador `BookTech-Setup.exe`.
* `sha256`: Hash SHA-256 criptográfico para validar que el ejecutable no fue alterado o corrompido.
* `notes`: Notas de lanzamiento mostradas al usuario en el diálogo de confirmación.

---

## 5. Cómo Generar una Nueva Release

1. Configura la nueva versión y notas de parche en `version.properties`.
2. Ejecuta desde PowerShell:
   ```powershell
   .\scripts\packaging\prepare-release.ps1 -RuntimePath "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
   ```
3. La carpeta lista para el usuario se generará en `dist/release_to_user/updates`.
