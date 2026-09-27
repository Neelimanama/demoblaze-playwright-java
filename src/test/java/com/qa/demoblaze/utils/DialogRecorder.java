package com.qa.demoblaze.utils;

import com.microsoft.playwright.Page;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Demoblaze reports most outcomes through native JavaScript alert() dialogs
 * ("Product added", "Wrong password.", ...). Playwright auto-dismisses dialogs unless a
 * handler is registered, so this class accepts every dialog and records its message.
 * <p>
 * Note: Playwright Java dispatches events only while a Playwright call is in progress, so
 * waiting uses {@link Page#waitForCondition} rather than a plain Java sleep/poll.
 */
public class DialogRecorder {

    private final Page page;
    private final List<String> messages = new CopyOnWriteArrayList<>();
    private int consumed = 0;

    public DialogRecorder(Page page) {
        this.page = page;
        page.onDialog(dialog -> {
            messages.add(dialog.message());
            dialog.accept();
        });
    }

    /** Waits for the next not-yet-consumed dialog and returns its message. */
    public String waitForNext(double timeoutMs) {
        final int index = consumed;
        page.waitForCondition(() -> messages.size() > index,
                new Page.WaitForConditionOptions().setTimeout(timeoutMs));
        consumed = index + 1;
        return messages.get(index);
    }

    public String waitForNext() {
        return waitForNext(10_000);
    }

    /** Messages received that no step has consumed yet. */
    public List<String> unconsumed() {
        return List.copyOf(messages.subList(consumed, messages.size()));
    }

    public List<String> all() {
        return List.copyOf(messages);
    }
}
