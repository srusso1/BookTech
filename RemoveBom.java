import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class RemoveBom {
    public static void main(String[] args) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get("src"))) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".java"))
                 .forEach(RemoveBom::removeBom);
        }
    }
    private static void removeBom(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length >= 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
                byte[] newBytes = new byte[bytes.length - 3];
                System.arraycopy(bytes, 3, newBytes, 0, newBytes.length);
                Files.write(path, newBytes);
                System.out.println("Removed BOM from " + path);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}