import os

replacements = {
    'Ã¡': 'á',
    'Ã©': 'é',
    'Ã\xad': 'í', 
    'Ã³': 'ó',
    'Ãº': 'ú',
    'Ã±': 'ñ',
    'Ã‘': 'Ñ',
    'Â¿': '¿',
    'Â¡': '¡',
    'ðŸ”¥': '🔥',
    'ðŸŽ¨': '🎨'
}

src_dir = r'c:\Users\SEBAS\Documents\GitHub\BookTech\src'
for root, dirs, files in os.walk(src_dir):
    for file in files:
        if file.endswith('.java') or file.endswith('.fxml'):
            filepath = os.path.join(root, file)
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                new_content = content
                for k, v in replacements.items():
                    new_content = new_content.replace(k, v)
                
                if new_content != content:
                    with open(filepath, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                    print(f'Fixed {filepath}')
            except Exception as e:
                pass
print('Done')
