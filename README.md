# An owner-record case study with Selenium and Java

An English-language educational QA project by [baruth91](https://github.com/baruth91),
shared with the QA community. Six independently authored browser cases connect
customer-record risks to Java code, explicit waits and repeatable execution.

## Verified first increment

All **six original cases passed on Windows and Ubuntu**, using headless Chrome.
[View the passing GitHub Actions run](https://github.com/baruth91/selenium-java-qa-case-study/actions/runs/40706015776)
and the [validation record](docs/validation.md) for exact scope and results.

## What is being tested?

A veterinary clinic needs reliable owner contact records. Can a staff member
create a record, read it after reloading, update the correct record, find it again
and receive useful feedback for invalid data or a missing search match?

The application is the official
[Spring Petclinic](https://github.com/spring-projects/spring-petclinic), downloaded
separately at a fixed revision from `upstream.properties`. The portfolio owns the
test code, rationale and lessons; it does not claim authorship of Petclinic or its
upstream tests. See [attribution](THIRD_PARTY.md).

## Run on Windows

Requirements: **JDK 21**, Git and installed Google Chrome. The Maven Wrapper
downloads Maven 3.9.16; a global Maven installation is unnecessary. Initial setup
and Selenium Manager's first driver resolution need internet access.

```powershell
git clone https://github.com/baruth91/selenium-java-qa-case-study.git
cd selenium-java-qa-case-study
java -version
java scripts/PrepareApp.java
.\mvnw.cmd -B test
```

For macOS/Linux, use `sh mvnw -B test` in the last step. The same source-file Java
setup command works on both systems. `JAVA_HOME` and the `java` command must refer
to a JDK 21 installation.

Preparation clones the fixed external application into ignored `.work/petclinic`
and builds its executable jar. It deliberately skips the upstream application's
tests while preparing the system under test; it does not skip our browser cases.
No Docker, paid browser cloud, external database or globally installed driver is
required for this initial increment.

## Watch a focused example

```powershell
.\mvnw.cmd -Dheadless=false -Dtest=OwnerWorkflowTest#createsOwnerAndReadsItAfterReload test
```

The suite starts its own real Spring Boot application, bound to loopback on an
OS-selected port. Each case opens a fresh Chrome session. The app uses H2 with
synthetic seed data, and each case generates its own unique owner surname.
The app process is stopped after the test class; browsers are closed after each
case. You do not need to start a server manually.

To explore the demo by hand separately:

```powershell
java -jar .work/petclinic/target/spring-petclinic-4.0.0-SNAPSHOT.jar --server.address=127.0.0.1 --server.port=8080
```

Then visit `http://127.0.0.1:8080` and stop it with Ctrl+C when finished. That manual
process is separate from the server created by the test suite.

## Cases and observable outcomes

| ID | Check | Outcome |
| --- | --- | --- |
| OWNER-01 | Create an owner and reload details | All intended contact fields survive a new page request |
| OWNER-02 | Edit the telephone and reload | Telephone changes while numeric record ID and name remain the same |
| OWNER-03 | Search a newly created unique surname | Search resolves to that exact record |
| OWNER-04a | Submit a blank first name | Visible field feedback; other input retained; form remains open |
| OWNER-04b | Submit a nonnumeric telephone | Visible ten-digit validation feedback; other input retained |
| OWNER-05 | Search an unknown surname | Explicit no-match feedback |

Five scenario methods produce **six executed cases** because invalid-input
coverage is parametrized. These checks do not cover all fields or domain rules.

## Read and learn

- [Case study and risks](docs/case-study.md)
- [Guided lesson and exercises](docs/learning-guide.md)
- [Actual validation results](docs/validation.md)
- [Business assertions](src/test/java/io/github/baruth91/OwnerWorkflowTest.java)
- [Page objects](src/test/java/io/github/baruth91/pages)
- [Application lifecycle](src/test/java/io/github/baruth91/support/AppServer.java)
- [Failure capture](src/test/java/io/github/baruth91/support/FailureEvidence.java)

Java records model immutable synthetic inputs. Small page objects describe page
interactions; JUnit tests retain business assertions. Selenium Manager resolves
ChromeDriver automatically. Explicit `WebDriverWait` conditions define readiness;
implicit waits are zero, and there are no fixed sleeps or automatic retries.

## Cloud execution and evidence

Open **Actions → Selenium owner workflow checks → Run workflow** on `main`.
The Ubuntu job builds the fixed application and runs all six original cases.
Pushes and pull requests also trigger it.

Surefire writes JUnit XML/text reports to `target/surefire-reports`. On failure,
the extension captures a screenshot, page HTML and context before quitting Chrome.
The application log is in `target/evidence/application.log`. Artifacts are retained
for seven days. A configured workflow is not a verified passing result: consult
the validation record.

## Important scope boundaries

Reload checks establish server-backed state within the running app. They do not
prove durability after application restart. Unique records isolate this suite's
scenarios logically; browser isolation alone does not reset the database. This
initial suite runs serially, on Chrome only.

Petclinic is a demo application. Authentication, authorization, production privacy,
pet/visit workflows, accessibility, load, and security testing are out of scope.
No real patient or personal data is used.

Related cases: [Playwright + Python](https://github.com/baruth91/playwright-python-qa-case-study)
and [Cypress + TypeScript](https://github.com/baruth91/cypress-typescript-qa-case-study).
