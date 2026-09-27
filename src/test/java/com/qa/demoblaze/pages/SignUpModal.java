package com.qa.demoblaze.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class SignUpModal {

    private final Locator modal;

    public SignUpModal(Page page) {
        this.modal = page.locator("#signInModal");
        modal.waitFor();
    }

    public SignUpModal enterCredentials(String username, String password) {
        modal.locator("#sign-username").fill(username);
        modal.locator("#sign-password").fill(password);
        return this;
    }

    public void submit() {
        modal.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Sign up")).click();
    }

    public Locator root() {
        return modal;
    }
}
