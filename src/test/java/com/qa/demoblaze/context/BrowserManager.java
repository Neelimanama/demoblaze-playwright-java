package com.qa.demoblaze.context;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.qa.demoblaze.config.Config;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Owns the expensive Playwright + Browser objects.
 * <p>
 * Playwright objects are not thread-safe, so each JUnit worker thread gets its own
 * Playwright/Browser pair (ThreadLocal). They are reused across tests on that thread and
 * closed once at the end of the run (see PlaywrightExtension). Isolation between tests comes from a fresh
 * BrowserContext per test (see {@link TestContext}), which is cheap.
 */
public final class BrowserManager {

    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final Queue<Playwright> ALL = new ConcurrentLinkedQueue<>();

    private BrowserManager() {
    }

    public static Playwright playwright() {
        Playwright pw = PLAYWRIGHT.get();
        if (pw == null) {
            pw = Playwright.create();
            PLAYWRIGHT.set(pw);
            ALL.add(pw);
        }
        return pw;
    }

    public static Browser browser() {
        Browser browser = BROWSER.get();
        if (browser == null || !browser.isConnected()) {
            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                    .setHeadless(Config.getBoolean("headless"))
                    .setSlowMo(Config.getInt("slow.mo"));
            browser = browserType(playwright()).launch(options);
            BROWSER.set(browser);
        }
        return browser;
    }

    private static BrowserType browserType(Playwright pw) {
        return switch (Config.get("browser").toLowerCase()) {
            case "firefox" -> pw.firefox();
            case "webkit" -> pw.webkit();
            case "chromium", "chrome" -> pw.chromium();
            default -> throw new IllegalArgumentException("Unsupported browser: " + Config.get("browser"));
        };
    }

    /** Called once when the whole JUnit run finishes (see PlaywrightExtension). Closing Playwright also closes its browsers. */
    public static void closeAll() {
        Playwright pw;
        while ((pw = ALL.poll()) != null) {
            try {
                pw.close();
            } catch (RuntimeException ignored) {
                // best effort shutdown
            }
        }
    }
}
