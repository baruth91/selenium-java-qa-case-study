package io.github.baruth91.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.baruth91.data.OwnerData;

public final class OwnerFormPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public OwnerFormPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("add-owner-form")));
    }

    public void fill(OwnerData data) {
        set("firstName", data.firstName());
        set("lastName", data.lastName());
        set("address", data.address());
        set("city", data.city());
        set("telephone", data.telephone());
    }

    private void set(String id, String value) {
        var input = driver.findElement(By.id(id));
        input.clear();
        input.sendKeys(value);
    }

    public void submit() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#add-owner-form button[type='submit']"))).click();
    }

    public String validationFor(String field) {
        By error = By.xpath("//*[@id='" + field + "']/ancestor::div[contains(@class,'form-group')][1]//span[@class='help-inline']");
        return wait.until(ExpectedConditions.visibilityOfElementLocated(error)).getText();
    }

    public String valueOf(String field) { return driver.findElement(By.id(field)).getDomProperty("value"); }
}
