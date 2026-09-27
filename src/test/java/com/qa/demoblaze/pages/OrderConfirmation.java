package com.qa.demoblaze.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.LinkedHashMap;
import java.util.Map;

/** The SweetAlert "Thank you for your purchase!" dialog shown after a purchase. */
public class OrderConfirmation {

    private final Page page;
    private final Locator dialog;

    public OrderConfirmation(Page page) {
        this.page = page;
        this.dialog = page.locator(".sweet-alert.visible");
    }

    public Locator root() {
        return dialog;
    }

    public Locator heading() {
        return dialog.locator("h2");
    }

    /**
     * Parses the body, which is rendered as lines of "Key: value":
     * Id, Amount ("1220 USD"), Card Number, Name, Date ("d/m/yyyy").
     */
    public Map<String, String> details() {
        Map<String, String> details = new LinkedHashMap<>();
        for (String line : dialog.locator("p.lead").innerText().split("\\R")) {
            int idx = line.indexOf(':');
            if (idx > 0) {
                details.put(line.substring(0, idx).strip(), line.substring(idx + 1).strip());
            }
        }
        return details;
    }

    public HomePage confirm() {
        page.waitForNavigation(() ->
                dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("OK")).click());
        return new HomePage(page);
    }
}
