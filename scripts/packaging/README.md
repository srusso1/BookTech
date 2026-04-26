# Empaquetado Windows (Launch4j + Inno Setup)

Este flujo genera:

- `dist/app/BookTech.exe` (ejecutable de Windows)
- `dist/installer/BookTech-Setup.exe` (instalador)
- runtime embebido dentro de `dist/app/runtime` (sin requerir Java instalado en cliente)

## 1) Requisitos en equipo de build

- JDK instalado (el mismo con el que compilas el proyecto)
- Launch4j instalado
- Inno Setup 6 instalado
- Runtime portable para distribuir (JRE/JDK) en carpeta local

## 2) Cambios de base de datos (ya aplicados)

La app usa `%LOCALAPPDATA%\\BookTech\\BookTechDB.db`.

- Si la DB no existe, se copia desde recursos (`/database/BookTechDB.db`).
- El instalador tambien intenta dejar una copia inicial en `%LOCALAPPDATA%\\BookTech`.

## 3) Build del paquete

Desde la raiz del proyecto:

```powershell
.\scripts\packaging\build-win.ps1 -RuntimePath "C:\runtimes\jdk-21-jre"
```

Si Launch4j o Inno Setup no estan en la ruta por defecto, indica ambas rutas:

```powershell
.\scripts\packaging\build-win.ps1 `
  -RuntimePath "C:\runtimes\jdk-21-jre" `
  -Launch4jExe "C:\Program Files (x86)\Launch4j\launch4jc.exe" `
  -InnoExe "C:\Program Files (x86)\Inno Setup 6\ISCC.exe"
```

## 4) Notas

- El instalador usa por defecto `C:\BookTech`.
- Para personalizar version, nombre o ruta del setup, edita `scripts/packaging/inno/BookTech.iss`.
- Para agregar icono `.ico` al exe, agrega la propiedad `<icon>` en `scripts/packaging/launch4j/booktech.xml`.

