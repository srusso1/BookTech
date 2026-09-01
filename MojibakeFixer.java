import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class MojibakeFixer {
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
            } else if (f.getName().endsWith(".java") || f.getName().endsWith(".fxml")) {
                String content = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                if (content.contains("Ã")) {
                    String fixed = fixMojibake(content);
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

    private static String fixMojibake(String text) {
        String current = text;
        for (int i = 0; i < 5; i++) {
            try {
                // If text was corrupted by reading UTF-8 as windows-1252,
                // the string has characters that map to cp1252 bytes.
                byte[] bytes = current.getBytes("windows-1252");
                String decoded = new String(bytes, StandardCharsets.UTF_8);
                
                // Only accept if it decodes cleanly and doesn't introduce replacement chars
                if (!decoded.contains("\uFFFD")) {
                    current = decoded;
                } else {
                    break; // Reached bottom or invalid
                }
            } catch (Exception e) {
                break; // Not windows-1252 encodeable
            }
        }
        return current;
    }
}