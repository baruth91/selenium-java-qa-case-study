import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Downloads the pinned external application and builds it; does not run upstream tests. */
class PrepareApp {
    public static void main(String[] args) throws Exception {
        Path root = Path.of("").toAbsolutePath();
        Properties config = new Properties();
        try (var input = Files.newInputStream(root.resolve("upstream.properties"))) {
            config.load(input);
        }
        Path app = root.resolve(config.getProperty("directory"));
        String revision = config.getProperty("revision");
        if (!Files.exists(app.resolve(".git"))) {
            run(root, List.of("git", "clone", "--no-checkout", config.getProperty("repository"), app.toString()));
            run(app, List.of("git", "checkout", "--detach", revision));
        }
        Process check = new ProcessBuilder("git", "rev-parse", "HEAD").directory(app.toFile()).start();
        String head = new String(check.getInputStream().readAllBytes()).trim();
        if (check.waitFor() != 0 || !head.equals(revision)) {
            throw new IllegalStateException("App revision differs from upstream.properties; inspect and preserve changes.");
        }
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        List<String> command = new ArrayList<>(windows
                ? List.of("cmd.exe", "/c", "mvnw.cmd") : List.of("sh", "mvnw"));
        command.addAll(List.of("-B", "-f", app.resolve("pom.xml").toString(),
                "-Dmaven.test.skip=true", "package"));
        run(root, command);
    }

    private static void run(Path directory, List<String> command) throws Exception {
        Process process = new ProcessBuilder(command).directory(directory.toFile()).inheritIO().start();
        if (process.waitFor() != 0) throw new IllegalStateException("Command failed: " + command.getFirst());
    }
}
