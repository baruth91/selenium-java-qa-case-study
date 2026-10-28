package io.github.baruth91.pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class OwnerSearchPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public OwnerSearchPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(baseUrl + "/owners/find");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("search-owner-form")));
    }

    public void search(String lastName) {
        driver.findElement(By.id("lastName")).sendKeys(lastName);
        driver.findElement(By.cssSelector("#search-owner-form button[type='submit']")).click();
    }

    public String noMatchMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#lastNameGroup .help-inline"))).getText();
    }
}
