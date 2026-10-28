# Validation record

## Initial target

- Java 21, Selenium 4.51.0 and JUnit Jupiter 5.13.4.
- Maven 3.9.16 via the Apache Maven Wrapper.
- Local: Windows and installed Chrome.
- CI: Ubuntu 24.04, Temurin 21 and installed Chrome.
- Six original cases in one test class, including two invalid-input variants.
- Fixed Spring Petclinic source revision in upstream.properties.

## Verified local baseline

On 2026-10-28, Windows with Temurin 21.0.12.1 and headless Chrome 156 completed
**6 tests, 0 failures, 0 errors, 0 skipped**. Surefire suite time: 21.02 seconds;
complete Maven command: 24.771 seconds. The real Petclinic jar was built from the
fixed source revision, started on loopback and stopped after the suite.

The JDK used for this initial validation was downloaded from the official
Adoptium API and its SHA-256 was verified. It lives in ignored .tools, not a global
installation or a committed binary. A user can run the documented commands with
their own JDK 21 installation; global Maven is not required.

## Findings and diagnostic evidence

The first execution had four passes and two assertion failures. Both failures
compared full URLs, which could include a transient servlet session matrix
parameter. The corrected tests compare the numeric record ID while retaining
the expected field assertions. These were test-oracle errors, not product defects.
The failure extension produced screenshots, HTML and context before teardown.

Selenium logged a CDP version warning for local Chrome 156 because the closest
bundled DevTools binding was 155. This suite uses standard WebDriver interactions,
not version-specific DevTools APIs, and all six cases passed. No CDP feature
compatibility is claimed and no warning was suppressed to obtain a pass.

Use `mvnw clean test` when a clean evidence directory is desired: ordinary `test`
does not delete diagnostic files left by earlier failures.

## Hosted CI

The Ubuntu execution will be recorded after its reports are verified. A published
workflow alone is not a passing CI result.
