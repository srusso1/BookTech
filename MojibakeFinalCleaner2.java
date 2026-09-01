import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class MojibakeFinalCleaner2 {
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
                
                fixed = fixed.replace("íƒÆ’í†â€™íƒâ€ í¢â‚¬â„¢íƒÆ’í¢â‚¬Å¡íƒâ€ší‚Â¢íƒÆ’í†â€™íƒâ€ší‚Â¢íƒÆ’í‚Â¢íƒÂ¢í¢â‚¬Å¡í‚Â¬íƒâ€¦í‚¡íƒÆ’í¢â‚¬Å¡íƒâ€ší‚Â¬íƒÆ’í†â€™íƒâ€ší‚Â¢íƒÆ’í‚Â¢íƒÂ¢í¢â€šÂ¬í…¡íƒâ€ší‚Â¬íƒÆ’í¢â‚¬Å¡íƒâ€ší‚Â ", "—");
                fixed = fixed.replace("íƒÆ’í‚Â°íƒâ€¦í‚Â¸íƒÂ¢í¢â€šÂ¬í‚Â íƒâ€ší‚Â¹", "🔹");
                fixed = fixed.replace("íƒÆ’í‚Â¢íƒÂ¢í¢â‚¬Å¡í‚Â¬íƒÂ¢í¢â€šÂ¬í‚Â ", "—");
                
                if (!fixed.equals(content)) {
                    Files.write(f.toPath(), fixed.getBytes(StandardCharsets.UTF_8));
                    System.out.println("Cleaned " + f.getPath());
                }
            }
        }
    }
}