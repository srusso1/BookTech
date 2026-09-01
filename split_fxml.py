import re

with open('src/main/resources/views/Rectoria/Configuracion.fxml', 'r', encoding='utf-8') as f:
    content = f.read()

imports = re.findall(r'<\?import.*?\?>', content)
print("Found imports:", len(imports))