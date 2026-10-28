package io.github.baruth91.support;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/** Capture before @AfterEach quits the browser; preserve the original failure. */
public final class FailureEvidence implements AfterTestExecutionCallback {
    private final Supplier<WebDriver> driver;

    public FailureEvidence(Supplier<WebDriver> driver) { this.driver = driver; }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty() || driver.get() == null) return;
        try {
            String name = context.getRequiredTestMethod().getName() + "-"
                    + Integer.toUnsignedString(context.getUniqueId().hashCode(), 16);
            Path folder = Path.of("target", "evidence", name);
            Files.createDirectories(folder);
            Files.write(folder.resolve("failure.png"), ((TakesScreenshot) driver.get()).getScreenshotAs(OutputType.BYTES));
            Files.writeString(folder.resolve("page.html"), driver.get().getPageSource());
            Files.writeString(folder.resolve("context.txt"), "URL: " + driver.get().getCurrentUrl()
                    + "\nTest: " + context.getDisplayName() + "\nFailure: " + context.getExecutionException().get());
        } catch (Exception evidenceError) {
            context.getExecutionException().get().addSuppressed(evidenceError);
        }
    }
}
