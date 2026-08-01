package com.landgo.userservice.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Normalizes the {@code htmlBody} an admin submits when publishing a newsletter.
 *
 * <p>The admin portal Base64-encodes the body; other clients may post raw HTML. Both are accepted.
 * Storing the encoded string verbatim would mail subscribers a wall of Base64, so decoding happens
 * once at publish time and the decoded markup is what gets persisted and sent.
 */
@Slf4j
public final class NewsletterHtmlBody {

    /** Base64 alphabet, allowing MIME line breaks and optional padding. */
    private static final String BASE64_PATTERN = "^[A-Za-z0-9+/\\s]+={0,2}$";

    private NewsletterHtmlBody() {
    }

    /**
     * Returns the body as renderable HTML.
     *
     * @param htmlBody raw HTML or Base64-encoded UTF-8 HTML; may be {@code null} or blank
     * @return decoded HTML when the input was Base64, otherwise the input unchanged
     */
    public static String decode(String htmlBody) {
        if (htmlBody == null || htmlBody.isBlank()) {
            return htmlBody;
        }

        String trimmed = htmlBody.trim();

        // Anything containing markup is already raw HTML. Only attempt a decode when the input
        // could plausibly be Base64, so an HTML snippet is never mangled by a lucky decode.
        if (trimmed.contains("<") || !trimmed.matches(BASE64_PATTERN)) {
            return htmlBody;
        }

        try {
            byte[] decodedBytes = Base64.getMimeDecoder().decode(trimmed);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
            log.debug("Decoded Base64 newsletter body ({} -> {} chars)", trimmed.length(), decoded.length());
            return decoded;
        } catch (IllegalArgumentException e) {
            log.warn("htmlBody matched the Base64 alphabet but could not be decoded; sending as-is");
            return htmlBody;
        }
    }
}
