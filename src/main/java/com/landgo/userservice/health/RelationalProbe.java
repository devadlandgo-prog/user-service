package com.landgo.userservice.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Relational probe: PostgreSQL.
 *
 * <p>Takes a connection from the pool and runs a validation query. Injecting
 * {@code DataSource} rather than a repository keeps this generated class
 * independent of the target's entity model.
 *
 * <p>This does not replace the existing actuator health indicator; it reports
 * separately so that a database blip cannot restart healthy instances.
 */
@Component
public class RelationalProbe implements DeepHealthProbe {

    private static final Logger log = LoggerFactory.getLogger(RelationalProbe.class);

    private static final int VALIDATION_TIMEOUT_SECONDS = 2;

    private final DataSource dataSource;

    public RelationalProbe(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String name() {
        return "postgres";
    }

    @Override
    public ProbeResult check() {
        Map<String, Object> details = new LinkedHashMap<>();
        long start = System.nanoTime();

        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(VALIDATION_TIMEOUT_SECONDS);
            details.put("connectionValid", valid);

            DatabaseMetaData metaData = connection.getMetaData();
            details.put("product", metaData.getDatabaseProductName());
            details.put("productVersion", metaData.getDatabaseProductVersion());

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT 1")) {
                details.put("query", resultSet.next());
            }

            if (!valid) {
                return ProbeResult.down(name(), millisSince(start),
                        "connection reported invalid", details);
            }
            return ProbeResult.up(name(), millisSince(start), details);
        } catch (Exception e) {
            log.warn("Relational probe failed", e);
            return ProbeResult.down(name(), millisSince(start), e.toString(), details);
        }
    }

    private static long millisSince(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
