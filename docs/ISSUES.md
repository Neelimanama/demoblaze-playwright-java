# Issues Log

Environment: https://www.demoblaze.com. Chromium 141 (Playwright 1.56), also checked by inspecting the site's JavaScript and its network traffic in the browser's developer tools. Found: 23 Sep 2026.

**Severity:** Critical = money or security at risk · High = core journey wrong · Medium = feature misbehaves, workaround exists · Low = cosmetic/content.

| ID | Title | Area | Severity | Found by |
|---|---|---|---|---|
| BUG-01 | Order confirmation date shows the wrong month | Checkout | High | Automated (TC04, tag `defect`) |
| BUG-02 | An order can be placed with an empty cart | Checkout | High | Automated (TC05, tag `defect`) |
| BUG-03 | Checkout accepts any card number and any expiry value | Checkout | Critical | Automated (TC05, tag `defect`) |
| SEC-01 | Session token is predictable (Base64 of username + number) | Auth | Critical | Exploratory / DevTools |
| SEC-02 | Full card number is shown back on the confirmation | Checkout | High | Automated (observed) |
---

### BUG-01 – Order confirmation date shows the wrong month
* **Steps:** Add any product → Cart → Place Order → enter Name and Credit card → Purchase.
* **Expected:** Date = today, e.g. `23/9/2026`.
* **Actual:** `23/8/2026`. The month is always one lower. In January it would show month `0`.
* **Cause (likely):** `cart.js` uses `date.getMonth()`, which is zero-based in JavaScript, without adding 1.
* **Evidence:** `mvn test -Pdefects` → *The order confirmation shows today's date*: `expected "23/9/2026" but was "23/8/2026"`.

### BUG-02 – An order can be placed with an empty cart
* **Steps:** Fresh session → Cart (empty) → Place Order → Name + Card → Purchase.
* **Expected:** Place Order is disabled, or an error says the cart is empty.
* **Actual:** "Thank you for your purchase!" with `Amount: 0 USD` and an order Id.
* **Impact:** Empty or phantom orders, misleading confirmation for the customer.

### BUG-03 – Checkout accepts any card number and any expiry value
* **Steps:** Add product → Place Order → Name `Jane Tester`, Credit card `not-a-card` (or `123`) → Purchase.
* **Expected:** Card number validated (digits only, valid length, Luhn check). Month and year validated. Country and city validated if required.
* **Actual:** Purchase succeeds. Only "Name" and "Credit card" are checked, and only for being non-empty. Month and year are free text (e.g. `99` / `abc`).
* **Impact:** Invalid payment data reaches the order. In a real store this means failed payments or orders fulfilled without a valid payment.


### SEC-01 – Session token is predictable
* The `tokenp_` cookie value is Base64 of `username + number`. For example, `cWFfcHJvYmVfMTQ3NzMyNzIxNjE3OTA3ODE=` decodes to `qa_probe_1477327216` followed by `1790781`. The number looks like an expiry value that can be guessed. The token is not signed, so it may be possible to forge a session for another user. This needs confirming by the security team.
* The cookie is set from JavaScript, so it cannot be `HttpOnly`, and it is not marked `Secure` or `SameSite`.

### SEC-02 – Full card number shown on the confirmation
* The confirmation shows the full PAN (`Card Number: 4111111111111111`). PCI DSS requires the displayed PAN to be masked (at most the first 6 and last 4 digits).

