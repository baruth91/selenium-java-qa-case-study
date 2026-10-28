package io.github.baruth91.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/** One real H2-backed app process per test class, on a port chosen by the OS. */
public final class AppServer implements AutoCloseable {
    private final Process process;
    private final CountDownLatch listening = new CountDownLatch(1);
    private volatile int port;

    public AppServer() throws Exception {
        Path appTarget = Path.of(".work", "petclinic", "target");
        if (!Files.isDirectory(appTarget)) {
            throw new IllegalStateException("Run java scripts/PrepareApp.java before running the tests.");
        }
        Path jar;
        try (var paths = Files.list(appTarget)) {
            jar = paths.filter(path -> path.getFileName().toString().matches("spring-petclinic-.*\\.jar"))
                    .findFirst().orElseThrow(() -> new IllegalStateException("Prepared Petclinic jar not found."));
        }
        Files.createDirectories(Path.of("target", "evidence"));
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", executable);
        process = new ProcessBuilder(java.toString(), "-jar", jar.toAbsolutePath().toString(),
                "--server.address=127.0.0.1", "--server.port=0", "--spring.main.banner-mode=off")
                .redirectErrorStream(true).start();
        Thread.ofPlatform().daemon().start(() -> {
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 var writer = Files.newBufferedWriter(Path.of("target", "evidence", "application.log"))) {
                for (String line; (line = reader.readLine()) != null;) {
                    writer.write(line);
                    writer.newLine();
                    writer.flush();
                    var match = Pattern.compile("Tomcat started on port (\\d+)").matcher(line);
                    if (match.find()) {
                        port = Integer.parseInt(match.group(1));
                        listening.countDown();
                    }
                }
            } catch (IOException ignored) {
                // Process shutdown can close the pipe; readiness failure still reports the log path.
            } finally {
                listening.countDown();
            }
        });
        if (!listening.await(90, TimeUnit.SECONDS) || port == 0) {
            close();
            throw new IllegalStateException("Petclinic did not start; inspect target/evidence/application.log.");
        }
        try (HttpClient client = HttpClient.newHttpClient()) {
            var response = client.send(HttpRequest.newBuilder(URI.create(baseUrl() + "/"))
                    .timeout(Duration.ofSeconds(20)).GET().build(), HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() != 200) {
                close();
                throw new IllegalStateException("Petclinic readiness returned HTTP " + response.statusCode());
            }
        } catch (Exception error) {
            close();
            throw error;
        }
    }

    public String baseUrl() { return "http://127.0.0.1:" + port; }

    @Override
    public void close() throws InterruptedException {
        process.destroy();
        if (!process.waitFor(15, TimeUnit.SECONDS)) {
            process.destroyForcibly().waitFor();
        }
    }
}
