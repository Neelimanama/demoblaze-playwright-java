package com.qa.demoblaze.context;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.qa.demoblaze.config.Config;
import com.qa.demoblaze.utils.DialogRecorder;

/**
 * Per-test browser state. {@link PlaywrightExtension} creates a new instance for every test,
 * so nothing (cookies, storage, alerts) leaks between tests.
 */
public class TestContext {

    private BrowserContext browserContext;
    private Page page;
    private DialogRecorder dialogs;

    public void start() {
        Browser browser = BrowserManager.browser();
        browserContext = browser.newContext(new Browser.NewContextOptions()
                .setBaseURL(Config.baseUrl())
                .setViewportSize(Config.getInt("viewport.width"), Config.getInt("viewport.height")));
        browserContext.setDefaultTimeout(Config.getInt("timeout.ms"));
        page = browserContext.newPage();
        dialogs = new DialogRecorder(page);
    }

    public BrowserContext browserContext() {
        return browserContext;
    }

    public Page page() {
        return page;
    }

    public DialogRecorder dialogs() {
        return dialogs;
    }

    public void close() {
        if (browserContext != null) {
            browserContext.close();
        }
    }
}
