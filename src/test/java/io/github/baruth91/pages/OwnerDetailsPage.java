package io.github.baruth91.pages;

import java.time.Duration;
import java.net.URI;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class OwnerDetailsPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public OwnerDetailsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[normalize-space()='Owner Information']")));
    }

    public String field(String label) {
        return driver.findElement(By.xpath("(//table)[1]//tr[th[normalize-space()='" + label + "']]/td")).getText();
    }

    public long ownerId() {
        // Servlet URL rewriting may append ;jsessionid before cookies are established.
        // The numeric record ID is the business identity; the session token is not.
        String path = URI.create(driver.getCurrentUrl()).getPath();
        var match = Pattern.compile("^/owners/(\\d+)(?:;[^/]*)?$").matcher(path);
        if (!match.matches()) throw new IllegalStateException("Not an owner-details URL: " + path);
        return Long.parseLong(match.group(1));
    }

    public OwnerFormPage edit() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Edit Owner"))).click();
        return new OwnerFormPage(driver);
    }
}
