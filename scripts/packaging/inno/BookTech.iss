; Inno Setup script para BookTech
[Setup]
AppId={{6EBD69AC-3560-4A2E-89D4-8E84C2A05028}
AppName=BookTech
AppVersion=1.0.0
AppPublisher=BookTech
SetupIconFile=..\..\..\src\main\resources\images\iconApp.ico
DefaultDirName={code:GetInstallDir}
DefaultGroupName=BookTech
DisableProgramGroupPage=yes
OutputDir=..\..\..\dist\installer
OutputBaseFilename=BookTech-Setup-{#AppVersion}
Compression=lzma
SolidCompression=yes
ArchitecturesInstallIn64BitMode=x64compatible
PrivilegesRequired=admin
WizardStyle=modern

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Tasks]
Name: "desktopicon"; Description: "Crear acceso directo en escritorio"; GroupDescription: "Accesos directos:"

[Files]
Source: "..\..\..\dist\app\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\..\..\dist\app\database\BookTechDB.db"; DestDir: "{localappdata}\BookTech\data"; Flags: ignoreversion onlyifdoesntexist uninsneveruninstall

[Dirs]
Name: "{app}\updates"
Name: "{app}\updates\win-x64\stable"

[Icons]
Name: "{autoprograms}\BookTech"; Filename: "{app}\BookTech.exe"
Name: "{autodesktop}\BookTech"; Filename: "{app}\BookTech.exe"; Tasks: desktopicon

[Run]
Filename: "{app}\BookTech.exe"; Description: "Abrir BookTech"; Flags: nowait postinstall skipifsilent

[Code]
function GetInstallDir(Param: String): String;
begin
  Result := ExpandConstant('{sd}\BookTech');
end;




