package com.qa.demoblaze.context;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
import com.qa.demoblaze.config.Config;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * JUnit 5 extension that gives every test a fresh {@link TestContext} (BrowserContext + Page),
 * records a Playwright trace, and on failure saves a full-page screenshot and the trace under
 * {@code target/}. Browsers are shared per thread and closed once, after the whole run.
 * <p>
 * Tests receive the context by declaring a {@code TestContext} parameter (see {@code BaseTest}).
 */
public class PlaywrightExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NS = ExtensionContext.Namespace.create(PlaywrightExtension.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        // Close every Playwright/Browser once, when the whole test run finishes.
        context.getRoot().getStore(NS).getOrComputeIfAbsent("browsers",
                k -> (AutoCloseable) BrowserManager::closeAll, AutoCloseable.class);

        TestContext ctx = new TestContext();
        ctx.start();
        if (!"off".equalsIgnoreCase(Config.get("trace"))) {
            ctx.browserContext().tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true).setSnapshots(true).setSources(false));
        }
        context.getStore(NS).put(TestContext.class, ctx);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        TestContext ctx = context.getStore(NS).remove(TestContext.class, TestContext.class);
        if (ctx == null) {
            return;
        }
        boolean failed = context.getExecutionException().isPresent();
        String name = fileName(context);
        try {
            Page page = ctx.page();
            if (failed && page != null && !page.isClosed()) {
                Path shot = Paths.get("target", "screenshots", name + ".png");
                page.screenshot(new Page.ScreenshotOptions().setFullPage(true).setPath(shot));
                System.out.println("Screenshot saved to " + shot.toAbsolutePath());
            }
            if (!ctx.dialogs().all().isEmpty()) {
                System.out.println("Browser alerts seen in " + context.getDisplayName() + ": " + ctx.dialogs().all());
            }
            stopTracing(ctx, failed, name);
        } finally {
            ctx.close();
        }
    }

    private void stopTracing(TestContext ctx, boolean failed, String name) {
        String mode = Config.get("trace").toLowerCase();
        if ("off".equals(mode)) {
            return;
        }
        Tracing.StopOptions options = new Tracing.StopOptions();
        if ("on".equals(mode) || failed) {
            Path trace = Paths.get("target", "traces", name + ".zip");
            options.setPath(trace);
            System.out.println("Trace saved to " + trace.toAbsolutePath() + " (open it at https://trace.playwright.dev)");
        }
        ctx.browserContext().tracing().stop(options);
    }

    /** e.g. CheckoutTest_invalidCardIsRejected_[1]_letters-123456789 */
    private static String fileName(ExtensionContext context) {
        String test = context.getRequiredTestClass().getSimpleName() + "_"
                + context.getRequiredTestMethod().getName() + "_" + context.getDisplayName();
        return test.replaceAll("[^A-Za-z0-9-_]+", "_") + "-" + System.nanoTime();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType() == TestContext.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return extensionContext.getStore(NS).get(TestContext.class, TestContext.class);
    }
}
