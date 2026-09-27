package com.qa.demoblaze.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class ProductPage extends BasePage {

    public ProductPage(Page page) {
        super(page);
    }

    public ProductPage waitForLoaded() {
        page.waitForURL("**/prod.html?idp_=*");
        title().waitFor();
        return this;
    }

    public Locator title() {
        return page.locator("#tbodyid h2.name");
    }

    /** Price text looks like "$360 *includes tax"; returns 360.0. */
    public double price() {
        String text = page.locator("#tbodyid h3.price-container").innerText();
        return Double.parseDouble(text.replaceAll("[^0-9.]+", " ").trim().split("\\s+")[0]);
    }

    /** Clicks "Add to cart" and waits for the backend call to complete. */
    public void addToCart() {
        page.waitForResponse(r -> r.url().endsWith("/addtocart"),
                () -> page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add to cart")).click());
    }
}
