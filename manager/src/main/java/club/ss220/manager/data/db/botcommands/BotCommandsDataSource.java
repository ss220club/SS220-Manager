package club.ss220.manager.data.db.botcommands;

import com.zaxxer.hikari.HikariDataSource;
import io.github.freya022.botcommands.api.core.db.HikariSourceSupplier;
import org.flywaydb.core.Flyway;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class BotCommandsDataSource implements HikariSourceSupplier {

    private static final String SCHEMA = "bc";
    private static final String MIGRATION_DIRECTORY = "bc_database_scripts";

    private final HikariDataSource source;

    public BotCommandsDataSource(@Qualifier("bcDataSource") DataSource persistenceDataSource) {
        if (!(persistenceDataSource instanceof HikariDataSource)) {
            throw new IllegalArgumentException("Expected HikariDataSource for persistence");
        }
        
        this.source = (HikariDataSource) persistenceDataSource;
        applyMigration();
    }

    private void applyMigration() {
        Flyway.configure()
                .dataSource(source)
                .schemas(SCHEMA)
                .locations(MIGRATION_DIRECTORY)
                .validateMigrationNaming(true)
                .loggers("slf4j")
                .load()
                .migrate();
    }

    @NotNull
    @Override
    public HikariDataSource getSource() {
        return source;
    }
}
