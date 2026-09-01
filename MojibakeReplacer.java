import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class MojibakeReplacer {
    public static void main(String[] args) throws Exception {
        int fixedCount = 0;
        File root = new File("src");
        fixedCount += processDirectory(root);
        System.out.println("Total fixed: " + fixedCount);
    }

    private static int processDirectory(File dir) throws Exception {
        int count = 0;
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                count += processDirectory(f);
            } else if (f.getName().endsWith(".java") || f.getName().endsWith(".fxml") || f.getName().endsWith(".xml")) {
                String content = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                if (content.contains("Ã") || content.contains("")) {
                    String fixed = content
                        // Level 4 corruption (worst)
                        .replace("ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³", "ó")
                        .replace("ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©", "é")
                        .replace("ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â°ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¹", "🔹")
                        .replace("ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚¡", "á")
                        .replace("ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­", "í")
                        .replace("ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â ", "—")
                        // Level 3 corruption
                        .replace("ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³", "ó")
                        .replace("ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â©", "é")
                        .replace("ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¡", "á")
                        .replace("ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â­", "í")
                        .replace("ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â Ãƒâ€šÃ‚Â¹", "🔹")
                        .replace("ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ", "—")
                        // Level 2 corruption
                        .replace("ÃƒÆ’Ã‚Â³", "ó")
                        .replace("ÃƒÆ’Ã‚Â©", "é")
                        .replace("ÃƒÆ’Ã‚Â¡", "á")
                        .replace("ÃƒÆ’Ã‚Â­", "í")
                        .replace("ÃƒÂ³", "ó")
                        .replace("ÃƒÂ©", "é")
                        .replace("ÃƒÂ¡", "á")
                        .replace("ÃƒÂ­", "í")
                        .replace("ÃƒÂ±", "ñ")
                        .replace("ÃƒÅ¡", "Ú")
                        .replace("Ã‚¿", "¿")
                        .replace("Ã¢â‚¬â€ ", "—")
                        .replace("Ã°Å¸â€ Â¹", "🔹")
                        .replace("Ã¢â€ â€™", "→")
                        // Level 1 corruption
                        .replace("Ã³", "ó")
                        .replace("Ã©", "é")
                        .replace("Ã¡", "á")
                        .replace("Ã­", "í")
                        .replace("Ã±", "ñ")
                        .replace("Ãº", "ú")
                        .replace("Ãš", "Ú")
                        .replace("Ã‰", "É")
                        .replace("Ã", "í") // If trailing Ã is left
                        ;

                    // Clean up any double replacements
                    fixed = fixed.replace("í³", "ó").replace("í©", "é").replace("í¡", "á").replace("í­", "í");

                    if (!fixed.equals(content)) {
                        Files.write(f.toPath(), fixed.getBytes(StandardCharsets.UTF_8));
                        System.out.println("Fixed " + f.getPath());
                        count++;
                    }
                }
            }
        }
        return count;
    }
}