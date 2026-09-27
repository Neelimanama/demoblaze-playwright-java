package com.qa.demoblaze.utils;

import java.util.UUID;

/** Test user credentials. Every test creates its own users so runs never collide. */
public record TestUser(String username, String password) {

    public static TestUser unique() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return new TestUser("qa_auto_" + suffix, "Pw!" + suffix);
    }
}
