# Automated Test Cases

There are five test cases (TC01–TC05) and each is implemented as a JUnit 5 test or parameterized test. The classes in [`src/test/java/com/qa/demoblaze/tests`](../src/test/java/com/qa/demoblaze/tests) are the executable version of this document.

| ID | Test class | Area | Tags | Result today |
|---|---|---|---|---|
| TC01 | `AccountTest` | Sign up → log in → log out | `smoke` | ✅ Pass |
| TC02 | `AccountTest` | Login rejected for invalid credentials (4 examples) | `negative` | ✅ Pass |
| TC03 | `CartTest` | Cart contents, prices and total, including delete | `smoke` | ✅ Pass |
| TC04 | `CheckoutTest` | Place order and check the confirmation | `smoke` (+ `defect` BUG-01) | ✅ Pass / ❌ BUG-01 |
| TC05 | `CheckoutTest` | Order blocked without mandatory data (3 examples) | `negative` (+ `defect` BUG-02, BUG-03) | ✅ Pass / ❌ BUG-02, BUG-03 |

The default run executes 10 tests, all passing. `mvn test -Pdefects` runs the 4 defect tests, which fail until the bugs are fixed.

---

## TC01 – A new user can sign up, log in and log out

**Test flow.** From the home page, register a new unique user through the Sign up modal and expect the alert "Sign up successful.". Log in with the same credentials. Expect "Welcome &lt;username&gt;" in the navigation bar, a Log out link, and the Log in and Sign up links hidden. Log out, and the navigation bar returns to the anonymous state.

**Why automate it.**
* This is the gateway to every personalised feature (cart tied to the account, orders). If it breaks, returning customers are blocked, so it belongs in the smoke pack for every build.
* It covers three state transitions (anonymous → registered → authenticated → anonymous) that are repetitive to check by hand and easy to break when front-end or session code changes.
* It uses a unique generated username each run, so it is repeatable against a shared environment without clean-up.

## TC02 – Login is rejected for invalid credentials (parameterized test)

| Example | Input | Expected alert |
|---|---|---|
| wrong password | existing user + wrong password | `Wrong password.` |
| user does not exist | never-registered username | `User does not exist.` |
| blank username | empty username | `Please fill out Username and Password.` |
| blank password | existing user + empty password | `Please fill out Username and Password.` |

The user must stay logged out in every case.

**Why automate it.**
* Negative authentication paths are a security and usability risk and are regularly broken by changes. Each example is quick to automate but slow to repeat by hand across browsers.
* A parameterized test covers the main equivalence classes (valid user with invalid password, unknown user, missing field) with one test body. Adding a new case means adding one row to the `@CsvSource` table.
* The "existing" user is registered through the UI at the start of each example, so every run is independent.

## TC03 – Cart lists the added products and keeps an accurate total

**Test flow.** Add one product from each category (Phones, Laptops, Monitors) through the UI. Check that each product page price matches the price shown in the product listing. Open the cart and check it contains exactly those products at those prices, and that the total equals the sum. Delete one product, then check the item count, that the product has gone, and that the total is recalculated.

**Why automate it.**
* The cart total is what the customer is charged, so an error here is a direct financial defect. Arithmetic checks are exactly what automation does better than a person.
* It covers the category filter, product page, add-to-cart and delete in one realistic journey across all three categories.
* The cart renders asynchronously (each row is loaded separately from the server) and totals are calculated in the browser. This is prone to race conditions, so repeatable automated runs are more likely to catch intermittent faults than occasional manual checks.
* The listing price is used as a second source for each price, so a wrong price on the product page would be caught rather than copied into the expected result.

## TC04 – A logged-in user places an order and receives an accurate confirmation

**Test flow.** *(Setup through the UI: register, log in, add two products to the cart.)* Open the cart, fill in the order form and click Purchase. The confirmation shows "Thank you for your purchase!", with the amount equal to the cart total and the correct name and card number. After confirming, the user returns to the home page and the cart is empty.

A second test, tagged `defect` and `BUG-01`, checks that the confirmation date is today's date. **It currently fails**: the month is shown one lower (for example 23/8/2026 on 23 September).

**Why automate it.**
* Checkout is the revenue-critical path, so it is the most important regression test in the suite.
* It checks data consistency across three places (cart total → confirmation amount → cart emptied afterwards). This would take a manual tester several minutes on every build.
* Adding to the cart is already covered in detail by TC03, so if TC03 passes and this test fails, the fault is most likely in checkout.

## TC05 – An order cannot be placed without the mandatory details (parameterized test)

| Example | Name | Card | Expected |
|---|---|---|---|
| missing name | blank | 4111… | alert `Please fill out Name and Creditcard.`, no confirmation |
| missing card | Jane Tester | blank | same |
| both missing | blank | blank | same |

Two related tests are tagged `defect`. They assert what a checkout **should** do, and **currently fail**:
* **BUG-02**: an order can be "placed" with an **empty cart** (confirmation shows Amount: 0 USD).
* **BUG-03**: an order is accepted with an **invalid card number** (`not-a-card`, `123`).

**Why automate it.**
* Validation rules are easy to break during refactoring and each combination is dull to re-check by hand. Automation covers them all on every run.
* The parameterized test uses equivalence partitioning on the two mandatory fields. The `defect` tests record the gaps found in exploratory testing as executable, repeatable checks, and they become regression tests once the defects are fixed.

---

## Considered but not automated (and why)

| Candidate | Decision |
|---|---|
| Pagination Next/Previous | Found defects manually (BUG-04). Would be the next test automated. It was left out to keep within the five-test limit, because the business impact is lower than checkout, cart and login. |
| Contact form | Low business impact. The main defect (no message is actually sent) is clearer as a manual finding. |
| About us video | A broken media source is a one-off visual/content check, so it is better as a manual or monitoring check. |
| Wrong product images | Visual/content data issue. Visual regression tooling would suit it better than functional assertions. |
