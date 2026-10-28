# Learn, demonstrate and extend this case

## 1. Start with the risk

Explain why reading data after a fresh page request is stronger than seeing the
values still present in the submitted form. Identify the exact evidence needed
to detect an edit applied to the wrong record.

Read `docs/case-study.md` before studying framework classes.

## 2. Prepare the application and run one test

Install JDK 21 and Chrome, then run `java scripts/PrepareApp.java` followed by the
focused command from the README. Explain the roles of JDK, Maven Wrapper,
Selenium, ChromeDriver, JUnit, Spring Boot and H2.

The wrapper pins Maven, while pom.xml pins direct dependencies. The Chrome and
JDK patch versions may differ across machines. Record actual versions with results
rather than claiming all environment components are frozen.

## 3. Read the business test before the page object

Open `OwnerWorkflowTest.java` and follow OWNER-01: generate data, create through
the UI, refresh, and assert the returned fields. Explain `assertAll`: it collects
the field comparisons into one useful result for this record check.

Then follow `OwnerFormPage` and `OwnerDetailsPage`. Their job is interaction and
data access, not deciding the expected business values.

## 4. Understand immutable data and parameters

`OwnerData` is a record. A changed phone produces a new data value rather than
mutating the original expectation. A unique surname prevents a search from
accidentally finding another test's owner.

OWNER-04 runs twice from CsvSource. Explain why one Java method can represent two
executed cases and why six passes do not mean every input boundary was covered.

## 5. Explain the browser lifecycle

`BeforeAll` starts the application; `BeforeEach` creates Chrome; `AfterEach` quits
the browser; `AfterAll` closes the app. Browsers must be closed even when a test
fails. The failure extension runs before teardown so evidence is still available.

An explicit wait asks for a condition, such as a visible destination heading.
Implicit waits are disabled to avoid mixing two timeout mechanisms. Increasing
a timeout without investigating the condition is not a complete diagnosis.

## 6. Practice a controlled failure

On a temporary branch, change the expected edited phone in OWNER-02 to an
incorrect value. Run only that test. Inspect its Surefire failure, screenshot,
HTML and context file under target. Restore the deliberate change before commit.

Classify failures as application, test, changed contract, driver/environment or
unresolved. A driver download problem is not evidence of broken owner editing.

## 7. Make one independent change

Add an exercise variant that edits city while preserving name and telephone.
State the expected result first, reuse the form interaction and assert the new
read after refresh. Explain what existing coverage misses before adding the case.

## Ten-minute demo

1. Product risk and current boundary (two minutes).
2. A visible owner-create/edit test (four minutes).
3. Explicit wait, record and browser cleanup (two minutes).
4. Cloud result and a diagnostic artifact (two minutes).

Completion means the learner can explain, modify and investigate the example.
Having an assistant create the project does not itself demonstrate that skill.
