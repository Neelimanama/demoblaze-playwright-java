package com.qa.demoblaze.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.Map;

public class PlaceOrderModal {

    private final Page page;
    private final Locator modal;

    public PlaceOrderModal(Page page) {
        this.page = page;
        this.modal = page.locator("#orderModal");
        modal.waitFor();
    }

    /** Keys: name, country, city, card, month, year (all optional; missing keys are left blank). */
    public PlaceOrderModal fill(Map<String, String> details) {
        for (String field : new String[]{"name", "country", "city", "card", "month", "year"}) {
            String value = details.get(field);
            if (value != null) {
                modal.locator("#" + field).fill(value);
            }
        }
        return this;
    }

    public void purchase() {
        modal.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Purchase")).click();
    }

    public Locator totalLabel() {
        return modal.locator("#totalm");
    }

    public OrderConfirmation confirmation() {
        return new OrderConfirmation(page);
    }
}
