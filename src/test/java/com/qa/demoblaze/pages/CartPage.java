package com.qa.demoblaze.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.LinkedHashMap;
import java.util.Map;

public class CartPage extends BasePage {

    public CartPage(Page page) {
        super(page);
    }

    public CartPage open() {
        page.waitForResponse(r -> r.url().endsWith("/viewcart"), () -> page.navigate("/cart.html"));
        return this;
    }

    public Locator rows() {
        return page.locator("#tbodyid tr");
    }

    public Locator total() {
        return page.locator("#totalp");
    }

    /** Title -> price, read from the rendered cart table. */
    public Map<String, Double> items() {
        Map<String, Double> items = new LinkedHashMap<>();
        for (Locator row : rows().all()) {
            String title = row.locator("td").nth(1).innerText().strip();
            double price = Double.parseDouble(row.locator("td").nth(2).innerText().strip());
            items.merge(title, price, Double::sum);
        }
        return items;
    }

    public void delete(String title) {
        Locator row = rows().filter(new Locator.FilterOptions().setHasText(title)).first();
        page.waitForNavigation(() -> row.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Delete")).click());
    }

    public PlaceOrderModal placeOrder() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Place Order")).click();
        return new PlaceOrderModal(page);
    }
}
