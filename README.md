# BookTech

Descripción
-----------
BookTech es una aplicación Java para la gestión bibliotecaria: catálogo de libros, préstamos, devoluciones, inventario y generación de informes. Está organizada por roles (Bibliotecario, Rectoría, Dashboard) y diseñada para desplegarse como una aplicación Java empaquetada con Maven.

Características principales
--------------------------
- Gestión de libros: registrar, editar, buscar y eliminar libros.
- Gestión de préstamos y devoluciones.
- Paneles y vistas por rol (Bibliotecario, Rectoría, Dashboard).
- Informes y estadísticas (exportables a formatos comunes).
- Scripts auxiliares para empaquetado y posibles instaladores en Windows.

Stack tecnológico
-----------------
- Lenguaje principal: Java
- Herramienta de build: Maven (se incluye el wrapper `mvnw`/`mvnw.cmd`)
- Scripts: PowerShell (automatización en Windows)
- Estilos estáticos: CSS
- Instalador opcional: Inno Setup (script .iss) para generar instaladores Windows

Requisitos
---------
- JDK 11 o superior (ajustar según lo indicado en `pom.xml` si aplica)
- Maven 3.6+ (puedes usar el wrapper incluido)
- (Opcional) Inno Setup para crear instaladores en Windows
- (Opcional) Docker para contenerización

Instalación y ejecución
-----------------------
1. Clonar el repositorio

```bash
git clone https://github.com/srusso1/BookTech.git
cd BookTech
```

2. Construir usando el wrapper (Linux/macOS)

```bash
./mvnw clean package
```

En Windows (PowerShell o cmd):

```powershell
mvnw.cmd clean package
```

3. Ejecutar la aplicación

```bash
java -jar target/booktech-<versión>.jar
```

Sustituye `<versión>` por la versión que indique el artifact generado en `target/`.

4. Ejecutar tests

```bash
./mvnw test
```

Ejecución desde el IDE
----------------------
- Importa el proyecto como Maven en IntelliJ IDEA, Eclipse o Visual Studio Code.
- Ejecuta la clase principal `application.App` o la configuración de ejecución que detecte el IDE.

Configuración
-------------
La configuración de la aplicación se encuentra en `src/main/resources` (archivos `.properties` o `.yml`). Las variables más comunes que podría necesitar la aplicación incluyen:

- APP_PORT (p. ej. 8080)
- DB_URL (jdbc:...)
- DB_USER
- DB_PASSWORD
- Rutas de archivos y claves externas (S3, API keys, etc.)

Añade los valores de configuración en un archivo `application.properties` o exporta variables de entorno según la práctica que adopte el proyecto.

Estructura del proyecto
-----------------------
```
.mvn/                       # Wrapper de Maven
mvnw, mvnw.cmd              # Maven wrapper
pom.xml                     # Archivo de build (dependencias y plugins)
version.properties          # Metadatos de versión
src/
  main/
    java/
      application/          # App.java, Bootstrap.java (arranque y configuración)
      controllers/          # Controladores agrupados por rol (Bibliotecario, Rectoria, Login, Dashboard)
      database/             # Capa de acceso a datos
      model/                # Entidades y modelos del dominio
      reports/              # Generación de informes
      utils/                # Utilitarios comunes
    resources/              # Recursos, plantillas, propiedades
scripts/                    # Scripts auxiliares (PowerShell, empaquetado)
updates/                    # Recursos de actualización
README.md                   # Este archivo
```

Puntos clave del código
-----------------------
- `src/main/java/application/App.java` — clase de arranque y bootstrap.
- `src/main/java/controllers/` — controladores por rol; por ejemplo `Bibliotecario/ConsultaController.java`, `Rectoria/InformesController.java`, `Login/LoginController.java`.
- `src/main/java/database/` — adaptadores de persistencia y acceso a la base de datos.
- `src/main/java/model/` — clases de dominio (Libro, Usuario, Préstamo, etc.).
- `src/main/java/reports/` — lógica para generar informes y exportarlos.

Empaquetado e instalador Windows
--------------------------------
- El proyecto se empaqueta con Maven en un JAR ejecutable (`target/`).
- Si existe un script de Inno Setup (`.iss`) en el repositorio, puedes compilarlo con ISCC (Inno Setup Compiler) para generar un instalador Windows:

```powershell
"C:\Program Files (x86)\Inno Setup 6\ISCC.exe" installer\booktech.iss
```

Scripts PowerShell
------------------
- Revisa `scripts/` para automatizar build, empaquetado o despliegue en Windows.
- Ejecuta los scripts con permisos adecuados en PowerShell: `./scripts/mi-script.ps1`.

Pruebas y calidad de código
---------------------------
- Ejecuta `./mvnw test` para correr la suite de pruebas unitarias.
- Para cobertura y calidad, integra plugins como JaCoCo, Checkstyle o SpotBugs en `pom.xml`.

Cómo contribuir
---------------
1. Haz fork del repositorio.
2. Crea una rama descriptiva: `git checkout -b feature/nombre-descriptivo`.
3. Haz commits atómicos y pruebas locales.
4. Abre un Pull Request describiendo los cambios, por qué son necesarios y cómo probarlos.

Issues y soporte
----------------
Al abrir un issue, incluye:
- Pasos para reproducir el problema
- Logs relevantes
- Versión de Java y sistema operativo
- Capturas de pantalla o dumps si aplica

Licencia
--------
Incluye un archivo `LICENSE` en la raíz con la licencia que prefieras (p. ej. MIT o Apache-2.0). Si no existe, añade una para dejar claro el uso permitido del proyecto.

Contacto
--------
Para dudas o soporte: abre un issue en el repositorio con la información solicitada arriba.

Badges recomendados
-------------------
- Estado del build (GitHub Actions)
- Licencia
- Cobertura de tests (JaCoCo)

Notas finales
-------------
He escrito este README en español y con instrucciones prácticas. Si quieres, puedo actualizarlo para incluir datos exactos extraídos de `pom.xml` (versión de Java, dependencias principales, plugin de empaquetado) y de `src/main/resources` (variables de configuración). Puedo aplicar esos cambios si me lo indicas o si autorizas que lea y modifique archivos adicionales del repositorio.
