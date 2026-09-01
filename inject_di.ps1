$files = Get-ChildItem -Recurse -Filter *Controller.java -Path src\main\java
foreach ($file in $files) {
    $content = Get-Content -Raw $file.FullName
    $className = $file.BaseName
    
    # Match the DAO fields: e.g. PrestamosDAO prestamosDAO = new PrestamosDAO();
    # or private final PrestamosDAO prestamosDAO = new PrestamosDAO();
    $regex = '(?m)^.*?(\w+DAO)\s+(\w+)\s*=\s*new\s+\w+DAO\(\)\s*;'
    $matches = [regex]::Matches($content, $regex)
    
    if ($matches.Count -gt 0) {
        $constructorParams = @()
        $constructorBody = @()
        
        foreach ($match in $matches) {
            $type = $match.Groups[1].Value
            $name = $match.Groups[2].Value
            $constructorParams += "$type $name"
            $constructorBody += "        this.$name = $name;"
            
            # Replace the declaration to be just private final without instantiation
            $content = $content -replace [regex]::Escape($match.Value), "    private final $type $name;"
        }
        
        $paramsJoined = $constructorParams -join ', '
        $bodyJoined = $constructorBody -join "
"
        
        $constructor = "    public $className($paramsJoined) {
$bodyJoined
    }

"
        
        # Inject constructor right before initialize() or the first @FXML method
        if ($content -match '(?m)^\s*@FXML') {
            # Find the index of the first @FXML
            $index = $content.IndexOf('@FXML')
            if ($index -ge 0) {
                # Find the previous newline to indent correctly
                $insertPos = $content.LastIndexOf("
", $index)
                if ($insertPos -lt 0) { $insertPos = $index }
                
                $content = $content.Insert($insertPos + 1, $constructor)
            }
        } elseif ($content -match '(?m)^\s*public void initialize') {
            $index = $content.IndexOf('public void initialize')
            $insertPos = $content.LastIndexOf("
", $index)
            $content = $content.Insert($insertPos + 1, $constructor)
        } else {
            # Just put it before the last closing brace
            $index = $content.LastIndexOf('}')
            $content = $content.Insert($index, "
" + $constructor)
        }
        
        [System.IO.File]::WriteAllText($file.FullName, $content, (New-Object System.Text.UTF8Encoding($false)))
    }
}