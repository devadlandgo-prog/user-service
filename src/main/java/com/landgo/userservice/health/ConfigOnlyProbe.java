package com.landgo.userservice.health;

import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reports whether an integration is configured, without calling it.
 *
 * <p>Checks every config key discovered for the capability, not one chosen key.
 * An integration is usually spread across several properties, of which only
 * some are populated in any given environment -- judging from a single
 * arbitrary key reports "not configured" for a perfectly working integration.
 *
 * <p>Values are never read into the response; only whether each key is set.
 */
public class ConfigOnlyProbe implements DeepHealthProbe {

    private final String name;
    private final Environment environment;
    private final List<String> keys;

    public ConfigOnlyProbe(String name, Environment environment, List<String> keys) {
        this.name = name;
        this.environment = environment;
        this.keys = keys;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ProbeResult check() {
        if (keys.isEmpty()) {
            return ProbeResult.skipped(name, "no configuration keys were discovered for this dependency");
        }

        List<String> populated = new ArrayList<>();
        for (String key : keys) {
            String value = environment.getProperty(key);
            if (value != null && !value.isBlank()) {
                populated.add(key);
            }
        }

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("depth", "config_only");
        details.put("keysChecked", keys.size());
        details.put("keysPopulated", populated);
        return ProbeResult.configured(name, !populated.isEmpty(), details);
    }
}
