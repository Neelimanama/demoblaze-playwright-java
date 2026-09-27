package com.qa.demoblaze.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;

public class HomePage extends BasePage {

    public HomePage(Page page) {
        super(page);
    }

    public HomePage open() {
        page.navigate("/index.html");
        waitForProducts();
        return this;
    }

    /** Products are rendered client-side after an API call, so wait for at least one card. */
    public HomePage waitForProducts() {
        productCards().first().waitFor();
        return this;
    }

    public Locator productCards() {
        return page.locator("#tbodyid .card");
    }

    public List<String> productTitles() {
        return page.locator("#tbodyid .card-title a").allInnerTexts().stream().map(String::strip).toList();
    }

    /** Category names as shown in the sidebar: Phones, Laptops, Monitors. */
    public HomePage selectCategory(String category) {
        page.waitForResponse(r -> r.url().endsWith("/bycat"),
                () -> page.locator(".list-group a", new Page.LocatorOptions().setHasText(category)).click());
        return this;
    }

    /** Price shown on the product card in the listing, e.g. "$360" -> 360.0. */
    public double listingPrice(String title) {
        Locator card = productCards().filter(new Locator.FilterOptions()
                .setHas(page.locator(".card-title a", new Page.LocatorOptions().setHasText(title)))).first();
        String text = card.locator("h5").innerText();
        return Double.parseDouble(text.replaceAll("[^0-9.]", ""));
    }

    public ProductPage openProduct(String title) {
        page.locator("#tbodyid .card-title a", new Page.LocatorOptions().setHasText(title)).first().click();
        return new ProductPage(page).waitForLoaded();
    }
}
