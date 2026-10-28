# Protecting the integrity of owner contact records

## Context and working hypotheses

A clinic depends on accurate owner identification and contact information. This
educational case focuses on creating, retrieving and editing owner records, not
clinical decisions or real medical/personal information.

The fixed Spring Petclinic revision defines the demo contract: owner names,
address and city are required; the telephone must contain ten digits. An exact
unique surname search redirects to the corresponding owner. These are observed
application rules, not universal requirements for international phone numbers.

## Risk-based scope

| Risk | Initial response | Limitation |
| --- | --- | --- |
| R1: entered owner details are lost or changed | OWNER-01 reads all fields after refresh | No application-restart durability claim |
| R2: an edit changes the wrong owner | OWNER-02 compares record ID, name and edited phone | No concurrent editing coverage |
| R3: staff cannot retrieve a known record | OWNER-03 searches a unique created surname | No partial, case-insensitive or paginated search coverage |
| R4: incomplete/unusable contact data is accepted | OWNER-04 has blank-name and nonnumeric-phone variants | Other fields and boundaries are not covered |
| R5: an empty search outcome is ambiguous | OWNER-05 checks explicit no-match feedback | No recovery transition in this increment |

## Data and environment

The external application is downloaded at the revision declared in
`upstream.properties`. Its Apache 2.0 license is preserved in that checkout.
The preparation command builds the app without running upstream tests; our suite
does not claim to be a validation of the upstream project's own tests.

The suite starts a real jar on a random local port. A log-derived startup event
and HTTP 200 readiness check gate browser execution. H2 data is initialized for
the application instance. Each scenario generates an unrelated synthetic owner
and never relies on another scenario's created ID. Tests run serially.

This is logical record isolation in one application instance. It is not a full
database reset between tests. Parallel execution or multiple classes sharing an
environment would require a deliberate data and lifecycle strategy.

## Architecture decisions

- Java 21 records keep expected input values immutable and separate from page code.
- JUnit 5 handles lifecycle, assertions, parameterized input and failure extensions.
- Selenium 4 and its built-in Manager resolve the browser driver without a
  committed binary, personal driver path or an additional manager dependency.
- Three small page objects represent actual screens. No inheritance hierarchy or
  generic click wrapper is introduced merely to make a framework look larger.
- Visible English field labels and stable input IDs provide locators. XPath is
  scoped to a labelled table row or the relevant field group where needed.
- Explicit waits target actionable controls, destination headings or field errors.
  They do not substitute a sleep for knowledge of the desired state.
- A JUnit extension captures failed state before browser teardown, retaining the
  original exception if evidence capture itself fails.

## Test oracles and limits

Expected values come from immutable test input, not the form's reflected values.
Refresh triggers a separate read of the created or edited record. Matching the
numeric record ID before and after an edit provides evidence of identity continuity.
This does not establish underlying database constraints or production identity
management.

During the first local run, comparing entire URLs caused two test failures because
the servlet container appended a transient `;jsessionid` on initial redirects.
The corrected oracle parses the numeric ID from an owner-details path, allowing
optional matrix parameters. It does not strip arbitrary digits from any URL or
treat a session token as business identity. All field assertions remain intact.

Negative cases assert feedback, preservation of the surname and remaining on the
form. They do not independently prove that no invalid database row was inserted.
A later API/database layer could provide that additional evidence if justified.

## Why this complements the other cases

The Playwright case teaches Python and browser/API assertions. The Cypress case
teaches TypeScript, controlled data and network synchronization. This case adds
Java's type system, Maven, WebDriver session management, explicit waits and JUnit
extensions against a different application domain.
