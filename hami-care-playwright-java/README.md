# Hami Care Admin — Playwright Java (POM + TestNG)

Automated end-to-end test suite for [Hami Care Admin Staging](https://staging.hami.care/auth/login) using **Playwright Java**, **TestNG**, and the **Page Object Model (POM)**.

## Project Structure

- `src/main/java/com/hami/care/config/ConfigReader.java` — Reads `config.properties`
- `src/main/java/com/hami/care/pages/` — Page Objects (`BasePage`, `LoginPage`, `UsersPage`, `DoctorApplicationsPage`, `ChildrenEldersPage`, `SubscriptionPlansPage`)
- `src/test/java/com/hami/care/base/BaseTest.java` — Playwright/Browser/Page lifecycle and screenshot capture on failure
- `src/test/java/com/hami/care/tests/` — TestNG test classes (`LoginTest`, `UsersTest`, `DoctorApplicationsTest`, `ChildrenEldersTest`, `SubscriptionPlansTest`)
- `src/test/resources/config.properties` — Environment URL and admin credentials

## Prerequisites

- Java 17+
- Maven 3.8+

## Setup & Run

```bash
# 1. Install Playwright browsers (first time only)
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"

# 2. Run the full TestNG suite
mvn test

# 3. Run a single test class (e.g. SubscriptionPlansTest)
mvn test -Dtest=SubscriptionPlansTest
```
