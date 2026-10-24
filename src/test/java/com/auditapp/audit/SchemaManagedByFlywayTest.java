package com.auditapp.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Every table and sequence must come from a Flyway migration. If this fails after adding an entity, write a
 * migration for it instead of letting Hibernate (or a schema.sql) create it, then add the table here.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:${random.uuid};MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH")
class SchemaManagedByFlywayTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void onlyFlywayCreatesTables() {
        List<String> tables = jdbc.queryForList("""
                select table_name from information_schema.tables
                where table_schema = 'public' and table_type = 'BASE TABLE'""", String.class);
        assertThat(tables).containsExactlyInAnyOrder(
                "flyway_schema_history", "audits", "findings", "organizations", "users", "memberships", "roles");
    }

    @Test
    void onlyFlywayCreatesSequences() {
        List<String> sequences = jdbc.queryForList(
                "select sequence_name from information_schema.sequences where sequence_schema = 'public'", String.class);
        assertThat(sequences).containsExactlyInAnyOrder("audit_seq", "finding_seq");
    }

    @Test
    void everyMigrationSucceeded() {
        // Rows without a version are Flyway's own bookkeeping, such as "<< Flyway Schema Creation >>".
        List<String> versions = jdbc.queryForList("""
                select version from flyway_schema_history
                where success and version is not null order by installed_rank""", String.class);
        assertThat(versions).containsExactly("1", "1.1", "2");
    }
}
