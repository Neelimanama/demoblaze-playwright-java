package com.qa.demoblaze.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Top navigation bar component (present on index, product and cart pages). */
public class NavBar {

    private final Page page;

    public NavBar(Page page) {
        this.page = page;
    }

    public Locator loginLink() {
        return page.locator("#login2");
    }

    public Locator signUpLink() {
        return page.locator("#signin2");
    }

    public Locator logoutLink() {
        return page.locator("#logout2");
    }

    public Locator welcomeUser() {
        return page.locator("#nameofuser");
    }

    public LoginModal openLogin() {
        loginLink().click();
        return new LoginModal(page);
    }

    public SignUpModal openSignUp() {
        signUpLink().click();
        return new SignUpModal(page);
    }

    public HomePage logOut() {
        logoutLink().click();
        page.waitForURL("**/index.html");
        return new HomePage(page);
    }

    public CartPage openCart() {
        page.locator("#cartur").click();
        page.waitForURL("**/cart.html");
        return new CartPage(page);
    }
}
