package com.example.innowise_vitali_payment_service.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.ext.mongodb.database.MongoConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Slf4j
@Configuration
public class LiquibaseConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.liquibase.change-log}")
    private String changeLog;

    @Value("${spring.liquibase.enabled:true}")
    private boolean enabled;

    @PostConstruct
    public void runLiquibase() {
        if (!enabled) {
            log.info("Liquibase migration step is disabled via configurations.");
            return;
        }

        log.info("Initializing Liquibase MongoDB schema migration updates...");

        try (MongoConnection connection = new MongoConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);

            try (Liquibase liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)) {
                liquibase.update("");
                log.info("Liquibase MongoDB changesets applied successfully without errors.");
            }
        } catch (Exception e) {
            log.error("Critical error during Liquibase MongoDB migration execution!", e);
            throw new RuntimeException("Database migration execution failed", e);
        }
    }
}