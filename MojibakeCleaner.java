import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class MojibakeCleaner {
    public static void main(String[] args) throws Exception {
        processDirectory(new File("src"));
    }

    private static void processDirectory(File dir) throws Exception {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                processDirectory(f);
            } else if (f.getName().endsWith(".java") || f.getName().endsWith(".fxml")) {
                String content = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                String fixed = content;
                
                // Remove all variants of the corrupted blue diamond (they all start with íƒÆ’ or similar and end with í‚Â¹ or similar)
                fixed = fixed.replaceAll("íƒÆ’í†â€™íƒâ€ í¢â‚¬â„¢íƒÆ’í¢â‚¬Å¡íƒâ€ší‚Â°.*?íƒÆ’í¢â‚¬Å¡íƒâ€ší‚Â¹", "🔹");
                fixed = fixed.replaceAll("ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°.*?ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¹", "🔹");
                
                // Em-dash corruptions
                fixed = fixed.replaceAll("íƒÆ’í†â€™íƒâ€ í¢â‚¬â„¢íƒÆ’í¢â‚¬Å¡íƒâ€ší‚Â¢.*?íƒÆ’í¢â‚¬Å¡íƒâ€ší‚Â ", "—");
                fixed = fixed.replaceAll("ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢.*?ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â ", "—");
                
                if (!fixed.equals(content)) {
                    Files.write(f.toPath(), fixed.getBytes(StandardCharsets.UTF_8));
                    System.out.println("Cleaned " + f.getPath());
                }
            }
        }
    }
}