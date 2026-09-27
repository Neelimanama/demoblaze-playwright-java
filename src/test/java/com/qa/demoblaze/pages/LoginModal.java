package com.qa.demoblaze.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginModal {

    private final Page page;
    private final Locator modal;

    public LoginModal(Page page) {
        this.page = page;
        this.modal = page.locator("#logInModal");
        modal.waitFor(); // Bootstrap fade-in: wait until visible before typing
    }

    public LoginModal enterCredentials(String username, String password) {
        modal.locator("#loginusername").fill(username);
        modal.locator("#loginpassword").fill(password);
        return this;
    }

    public void submit() {
        modal.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Log in")).click();
    }

    /** Submits the login and waits for the logged-in navbar to appear. */
    public HomePage submitExpectingSuccess() {
        submit();
        // Wait until the navbar shows "Welcome <username>" after login
        assertThat(page.locator("#nameofuser")).containsText("Welcome");
        return new HomePage(page);
    }

}
