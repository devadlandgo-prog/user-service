package com.landgo.userservice.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NewsletterHtmlBodyTest {

    @Test
    @DisplayName("decodes the Base64 body the admin portal sends")
    void decodesBase64FromAdminPortal() {
        // The exact payload from the API handoff document.
        assertEquals("<h1>Hello</h1>", NewsletterHtmlBody.decode("PGgxPkhlbGxvPC9oMT4="));
    }

    @Test
    @DisplayName("leaves raw HTML untouched")
    void passesRawHtmlThrough() {
        String raw = "<div class=\"space-y-5\"><p>We use cookies...</p></div>";
        assertEquals(raw, NewsletterHtmlBody.decode(raw));
    }

    @Test
    @DisplayName("preserves UTF-8 characters through a decode")
    void preservesUtf8() {
        String html = "<p>Terrains à vendre — 5 000 $</p>";
        String encoded = Base64.getEncoder().encodeToString(html.getBytes(StandardCharsets.UTF_8));

        assertEquals(html, NewsletterHtmlBody.decode(encoded));
    }

    @Test
    @DisplayName("leaves plain text that is not valid Base64 alone")
    void leavesPlainTextAlone() {
        assertEquals("Hello there!", NewsletterHtmlBody.decode("Hello there!"));
    }

    @Test
    @DisplayName("handles null and blank bodies without throwing")
    void handlesEmptyInput() {
        assertNull(NewsletterHtmlBody.decode(null));
        assertEquals("   ", NewsletterHtmlBody.decode("   "));
    }
}
