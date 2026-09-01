import os

def fix_file(filepath, replacements):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    for old, new in replacements:
        content = content.replace(old, new)
        
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

base_path = r'c:\Users\SEBAS\Documents\GitHub\BookTech\src\main\java\controllers\Bibliotecario'

fix_file(os.path.join(base_path, 'ConsultaController.java'), [
    (' íƒÆ’í†â€™íƒâ€ í¢â‚¬â„¢íƒÆ’í¢â‚¬Å¡íƒâ€ší‚Â¢íƒÆ’í†â€™íƒâ€ší‚Â¢íƒÆ’í‚Â¢íƒÂ¢í¢â‚¬Å¡í‚Â¬íƒâ€¦í‚¡íƒÆ’í¢â‚¬Å¡íƒâ€ší‚Â¬íƒÆ’í†â€™íƒâ€ší‚Â¢íƒÆ’í‚Â¢íƒÂ¢í¢â€šÂ¬í…¡íƒâ€ší‚Â¬íƒÆ’í¢â‚¬Å¡íƒâ€ší‚Â ', '-')
])

fix_file(os.path.join(base_path, 'PrestamoController.java'), [
    ('íƒÆ’í‚Â°íƒâ€¦í‚Â¸íƒÂ¢í¢â€šÂ¬í‚Â íƒâ€ší‚Â¹ ', ''),
    (' íƒÆ’í‚Â¢íƒÂ¢í¢â‚¬Å¡í‚Â¬íƒÂ¢í¢â€šÂ¬í‚Â ', '-')
])

print('Done replacing.')
