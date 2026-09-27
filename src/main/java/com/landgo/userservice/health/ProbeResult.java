package com.landgo.userservice.health;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Outcome of a single dependency probe.
 *
 * <p>{@code SKIPPED} and {@code CONFIGURED} are deliberately not failures.
 * A probe that could not run, or one that only reports configuration state,
 * must not drag the endpoint to DOWN -- only {@code DOWN} means "this
 * dependency is broken".
 */
public final class ProbeResult {

    public static final String UP = "UP";
    public static final String DOWN = "DOWN";
    public static final String SKIPPED = "SKIPPED";
    public static final String CONFIGURED = "CONFIGURED";
    public static final String NOT_CONFIGURED = "NOT_CONFIGURED";

    private final String name;
    private final String status;
    private final long latencyMs;
    private final Map<String, Object> details;

    private ProbeResult(String name, String status, long latencyMs, Map<String, Object> details) {
        this.name = name;
        this.status = status;
        this.latencyMs = latencyMs;
        this.details = details == null ? new LinkedHashMap<>() : details;
    }

    public static ProbeResult up(String name, long latencyMs, Map<String, Object> details) {
        return new ProbeResult(name, UP, latencyMs, details);
    }

    public static ProbeResult down(String name, long latencyMs, String error, Map<String, Object> details) {
        Map<String, Object> merged = details == null ? new LinkedHashMap<>() : new LinkedHashMap<>(details);
        merged.put("error", error);
        return new ProbeResult(name, DOWN, latencyMs, merged);
    }

    public static ProbeResult skipped(String name, String reason) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("reason", reason);
        return new ProbeResult(name, SKIPPED, 0L, details);
    }

    public static ProbeResult configured(String name, boolean present, Map<String, Object> details) {
        return new ProbeResult(name, present ? CONFIGURED : NOT_CONFIGURED, 0L, details);
    }

    public boolean isDown() {
        return DOWN.equals(status);
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
