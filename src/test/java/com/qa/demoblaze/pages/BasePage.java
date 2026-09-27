package com.qa.demoblaze.pages;

import com.microsoft.playwright.Page;

/** Common behaviour for every Demoblaze page: all pages share the same navigation bar. */
public abstract class BasePage {

    protected final Page page;
    protected final NavBar nav;

    protected BasePage(Page page) {
        this.page = page;
        this.nav = new NavBar(page);
    }

    public NavBar nav() {
        return nav;
    }

    public Page page() {
        return page;
    }
}
