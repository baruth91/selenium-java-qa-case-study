package io.github.baruth91;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.baruth91.data.OwnerData;
import io.github.baruth91.pages.OwnerDetailsPage;
import io.github.baruth91.pages.OwnerFormPage;
import io.github.baruth91.pages.OwnerSearchPage;
import io.github.baruth91.support.AppServer;
import io.github.baruth91.support.FailureEvidence;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OwnerWorkflowTest {
    private AppServer app;
    private WebDriver driver;

    @RegisterExtension
    final FailureEvidence evidence = new FailureEvidence(() -> driver);

    @BeforeAll
    void startApplication() throws Exception { app = new AppServer(); }

    @AfterAll
    void stopApplication() throws Exception { if (app != null) app.close(); }

    @BeforeEach
    void openFreshBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("headless", "true"))) options.addArguments("--headless=new");
        options.addArguments("--window-size=1440,1000", "--lang=en-US");
        options.setExperimentalOption("prefs", Map.of("intl.accept_languages", "en-US,en"));
        driver = new ChromeDriver(options); // Selenium Manager resolves the driver.
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
    }

    @AfterEach
    void closeBrowser() { if (driver != null) { driver.quit(); driver = null; } }

    private OwnerDetailsPage create(OwnerData owner) {
        driver.get(app.baseUrl() + "/owners/new");
        var form = new OwnerFormPage(driver);
        form.fill(owner);
        form.submit();
        return new OwnerDetailsPage(driver);
    }

    @Test
    @DisplayName("OWNER-01: new owner data remains visible after a fresh page request")
    void createsOwnerAndReadsItAfterReload() {
        OwnerData owner = OwnerData.unique();
        create(owner);
        driver.navigate().refresh();
        var details = new OwnerDetailsPage(driver);
        assertAll(
                () -> assertEquals(owner.firstName() + " " + owner.lastName(), details.field("Name")),
                () -> assertEquals(owner.address(), details.field("Address")),
                () -> assertEquals(owner.city(), details.field("City")),
                () -> assertEquals(owner.telephone(), details.field("Telephone")));
    }

    @Test
    @DisplayName("OWNER-02: editing a telephone preserves the owner's identity")
    void updatesContactWithoutChangingIdentity() {
        OwnerData owner = OwnerData.unique();
        var details = create(owner);
        long originalId = details.ownerId();
        var form = details.edit();
        form.fill(owner.withTelephone("2025550199"));
        form.submit();
        new OwnerDetailsPage(driver);
        driver.navigate().refresh();
        var updated = new OwnerDetailsPage(driver);
        assertAll(
                () -> assertEquals(originalId, updated.ownerId()),
                () -> assertEquals("2025550199", updated.field("Telephone")),
                () -> assertEquals(owner.firstName() + " " + owner.lastName(), updated.field("Name")));
    }

    @Test
    @DisplayName("OWNER-03: exact surname search finds the independently created record")
    void findsCreatedOwner() {
        OwnerData owner = OwnerData.unique();
        long expectedId = create(owner).ownerId();
        var search = new OwnerSearchPage(driver, app.baseUrl());
        search.search(owner.lastName());
        var found = new OwnerDetailsPage(driver);
        assertEquals(expectedId, found.ownerId());
        assertEquals(owner.firstName() + " " + owner.lastName(), found.field("Name"));
    }

    @ParameterizedTest(name = "OWNER-04: rejects invalid {0}")
    @CsvSource({"firstName, must not be blank", "telephone, must be a 10-digit number"})
    void rejectsInvalidOwnerInput(String field, String expectedError) {
        OwnerData valid = OwnerData.unique();
        OwnerData invalid = field.equals("firstName") ? valid.withFirstName("") : valid.withTelephone("abc");
        driver.get(app.baseUrl() + "/owners/new");
        var form = new OwnerFormPage(driver);
        form.fill(invalid);
        form.submit();
        assertAll(
                () -> assertTrue(form.validationFor(field).contains(expectedError)),
                () -> assertEquals(valid.lastName(), form.valueOf("lastName")),
                () -> assertTrue(driver.getCurrentUrl().endsWith("/owners/new")));
    }

    @Test
    @DisplayName("OWNER-05: an unknown surname produces explicit no-match feedback")
    void reportsNoSearchMatch() {
        var search = new OwnerSearchPage(driver, app.baseUrl());
        search.search(OwnerData.unique().lastName());
        assertTrue(search.noMatchMessage().contains("has not been found"));
    }
}
