package com.landgo.userservice.config;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiquibaseConfig {

    @Bean
    @ConfigurationProperties("app.liquibase.public")
    public LiquibaseProperties publicLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    @ConfigurationProperties("app.liquibase.users")
    public LiquibaseProperties usersLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    public SpringLiquibase publicLiquibase(
            DataSource dataSource,
            @Qualifier("publicLiquibaseProperties") LiquibaseProperties properties) {
        return buildLiquibase(dataSource, properties);
    }

    @Bean
    public SpringLiquibase usersLiquibase(
            DataSource dataSource,
            @Qualifier("usersLiquibaseProperties") LiquibaseProperties properties) {
        return buildLiquibase(dataSource, properties);
    }

    private SpringLiquibase buildLiquibase(DataSource dataSource, LiquibaseProperties properties) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setShouldRun(properties.isEnabled());
        liquibase.setChangeLog(properties.getChangeLog());
        liquibase.setContexts(properties.getContexts());
        liquibase.setDefaultSchema(properties.getDefaultSchema());
        liquibase.setLiquibaseSchema(properties.getLiquibaseSchema());
        liquibase.setDatabaseChangeLogTable(properties.getDatabaseChangeLogTable());
        liquibase.setDatabaseChangeLogLockTable(properties.getDatabaseChangeLogLockTable());
        liquibase.setDropFirst(properties.isDropFirst());
        return liquibase;
    }
}
