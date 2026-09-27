# Test Plan – Demoblaze (Product Store)

| | |
|---|---|
| **System under test** | https://www.demoblaze.com |
| **Objective** | Show that a user can register, log in, build a cart and place an order, and that the prices and totals are right. Find and report defects. |
| **Scope of this exercise** | Functional testing of the customer journeys|

---

## 1. What the site does and where the risk is

The site is a small e-commerce store with these functional areas:

| Area | Features | Business impact if broken | Likelihood of defects | Priority |
|---|---|---|---|---|
| **Checkout / Place order** | Order form, validation, confirmation, cart emptied afterwards | **Very high**: lost revenue, wrong charges, customer complaints | High (client-side-only validation, confirmation built in the browser) | **P1** |
| **Cart** | Add item, list items, delete item, running total | **Very high**: wrong totals mean wrong charges | Medium (total calculated in JS, rows load asynchronously) | **P1** |
| **Authentication** | Sign up, log in, log out, session cookie | High: blocks returning customers; security exposure | Medium | **P1** |
| **Catalogue** | Category filter, pagination, product details page | Medium: shoppers can't find products | Medium–High | **P2** |
| **Contact / About us** | Contact form, video modal | Low | High | **P3** |

## 2. Types of functional testing 

1. **End-to-end journey (happy path) tests**: *browse → add to cart → checkout → confirmation*, and *sign up → log in → log out*. If these fail, the site cannot trade. They are the smoke tests for every build.
2. **Business-rule / calculation tests**: the cart total equals the sum of the line items, the confirmation amount equals the cart total, and the product page price equals the catalogue price. Mistakes here cost money, and people rarely spot them by eye.
3. **Negative and validation tests**: invalid login, missing mandatory checkout fields, empty cart, invalid card data. Most real defects are found here, and the brief warns that the site contains deliberate bugs.
4. **State and data-integrity tests**: the cart is empty after purchase, deleting an item updates the total, and a session persists after reload and is cleared on logout.
5. **Boundary / equivalence partitioning** on inputs such as blank, whitespace, very long, special-character and case-variant usernames, and short or non-numeric card numbers.
6. **Exploratory testing**, time-boxed per area, to find issues that scripted tests miss: pagination, navigation, broken media, error handling for bad URLs.
7. **Light cross-browser checks** (Chromium, Firefox, WebKit). The framework switches browser with one property.

**Out of scope for this exercise:** performance/load testing , full accessibility audit , security penetration testing, and visual regression. 

## 3. Test strategy and approach 

* **Risk-based and journey-first.** A small number of high-value tests gives more confidence than a large number of shallow ones. The five automated test cases cover the P1 areas. P2 and P3 areas were tested manually and exploratorily, and the findings are in the issues log.
* **Manual exploration first, then automation.** I explored each area first. This showed how the site behaves: native `alert()` dialogs, asynchronous rendering, and client-side-only validation. The automation is built on what the exploration found.
* **Automate what is repeatable and high-value.** Automation targets checks that must run on every change (regression), involve arithmetic, or need many data combinations (parameterized tests). One-off visual checks (such as a wrong product image or a broken video) are recorded as manual findings rather than automated.
* **UI only, end to end.** Users are registered, logged in and carts filled through the UI, exactly as a shopper would. Tests stay independent because each one registers its own fresh user. Product page prices are cross-checked against the price shown in the product listing.
* **Isolation.** Every test gets a fresh browser context and its own newly registered users, so tests can run in any order and in parallel.
* **Tests that expose defects fail.** Tests that assert correct behaviour but currently fail are tagged `defect` plus a bug ID, and are excluded from the default regression run so the pipeline stays a useful signal. They run with `mvn test -Pdefects`, and once a defect is fixed its tag is removed.

## 4. Tools and frameworks considered

| Need | Chosen | Alternatives considered | Why |
|---|---|---|---|
| Browser automation | **Playwright for Java** | Selenium WebDriver, Cypress, Playwright TS | Auto-waiting and web-first assertions reduce flakiness on an asynchronous site like this. It has built-in dialog handling (the site uses `alert()` everywhere), Trace Viewer for debugging, and supports Chromium, Firefox and WebKit. Cypress is JavaScript-only and has limitations with multiple origins (the site's pages load data from a second domain, `api.demoblaze.com`). Selenium needs explicit waits and more plumbing. |
| Language | **Java 17** | TypeScript | A common choice in enterprise and government test teams. It is strongly typed and works well with Maven-based CI. |
| Test framework | **JUnit 5** (`@Test`, `@ParameterizedTest`, `@Tag`) + Maven Surefire | TestNG | Standard in Java teams. Tag filtering (`-Dgroups`) selects smoke, negative or defect tests, `@ParameterizedTest` covers data-driven negative cases in one test, and parallel execution is built in. |
| Assertions | **Playwright assertions** (UI, auto-retry) + **AssertJ** (data) | Hamcrest | Retrying UI assertions avoid timing flakiness. AssertJ gives clear failure messages for data checks. |
| Test data | Generated per test (`TestUser.unique()`), created through the UI | Fixed shared test accounts | Unique users mean tests never collide, even in parallel, and no clean-up is needed between runs. |
| Reporting | Surefire JUnit XML/text reports, screenshots and Playwright traces on failure | Allure, Extent | No extra infrastructure needed. Allure could be added with one plugin. |

## 5. Test environment and data

* Public test site. No control over data, so tests create their own users (`qa_auto_<random>`) and never depend on existing accounts.
* Default browser Chromium (headless). Firefox and WebKit via `-Dbrowser=firefox|webkit`.
* The site is shared and publicly used, so tests avoid fixed usernames and fixed sleeps. They wait on network responses and use retrying assertions instead.

## 6. Entry / exit criteria

* **Entry:** site reachable. Smoke tests (tag `smoke`) pass.
* **Exit (for a release):** all P1 tests pass. No open Critical or High defects without an agreed workaround. All defects logged with reproduction steps.

## 7. Deliverables

* This test plan
* Automated test cases (TEST_CASES.md)
* ISSUES.md
* Automation framework source code (see ReadMe.md)
