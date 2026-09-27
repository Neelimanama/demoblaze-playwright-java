# Demoblaze – Functional Test Automation (Playwright + JUnit 5, Java)

Test plan, automated tests and issues log for the Senior Test Engineer assessment against
[demoblaze.com](https://www.demoblaze.com/index.html).

| Deliverable | Where |
|---|---|
| 1. Test plan (priorities, rationale, tools) | [docs/TEST_PLAN.md](docs/TEST_PLAN.md) |
| 2. Automated test cases and why each was automated | [docs/TEST_CASES.md](docs/TEST_CASES.md) and [test classes](src/test/java/com/qa/demoblaze/tests) |
| 3. Issues found | [docs/ISSUES.md](docs/ISSUES.md) |

## Tech stack

Java 17 · Playwright for Java 1.56 · JUnit 5 (Jupiter, parameterized tests) · AssertJ · Maven ·
The reasons for each choice are in the [test plan](docs/TEST_PLAN.md#4-tools-and-frameworks-considered).

## Quick start

Prerequisites: **JDK 17+** and **Maven 3.9+**. Playwright downloads its browsers on the first run.

```bash
# Regression pack (10 tests, all passing), headless Chromium, 3 tests in parallel
mvn test

# Tests that demonstrate known defects (these FAIL on purpose until the bugs are fixed)
mvn test -Pdefects

# Everything
mvn test -Pall

# One test case or group (JUnit @Tag values)
mvn test -Dgroups=TC03
mvn test -Dgroups=smoke

# One test class or method
mvn test -Dtest=CartTest
mvn test -Dtest=CheckoutTest#loggedInUserPlacesOrderWithAccurateConfirmation

# Another browser / sequential run
mvn test -Dbrowser=firefox
mvn test -Djunit.jupiter.execution.parallel.enabled=false
```
