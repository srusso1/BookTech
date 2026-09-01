import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.Map;
import java.util.HashMap;

public class scratch_fix_enc {
    public static void main(String[] args) throws IOException {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("Ã¡", "á");
        replacements.put("Ã©", "é");
        replacements.put("Ã\u00AD", "í"); 
        replacements.put("Ã³", "ó");
        replacements.put("Ãº", "ú");
        replacements.put("Ã±", "ñ");
        replacements.put("Ã‘", "Ñ");
        replacements.put("Â¿", "¿");
        replacements.put("Â¡", "¡");
        replacements.put("ðŸ”¥", "🔥");
        replacements.put("ðŸŽ¨", "🎨");
        
        Path srcDir = Paths.get("src");
        try (Stream<Path> paths = Files.walk(srcDir)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".java") || p.toString().endsWith(".fxml"))
                 .forEach(p -> {
                     try {
                         String content = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                         String newContent = content;
                         for (Map.Entry<String, String> entry : replacements.entrySet()) {
                             newContent = newContent.replace(entry.getKey(), entry.getValue());
                         }
                         if (!newContent.equals(content)) {
                             Files.write(p, newContent.getBytes(StandardCharsets.UTF_8));
                             System.out.println("Fixed " + p);
                         }
                     } catch (IOException e) {
                         System.err.println("Failed " + p);
                     }
                 });
        }
        System.out.println("Done");
    }
}
