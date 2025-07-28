package club.ss220.manager.data.db.botcommands;

import com.zaxxer.hikari.HikariDataSource;
import io.github.freya022.botcommands.api.core.db.HikariSourceSupplier;
import org.flywaydb.core.Flyway;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class PersistenceSource implements HikariSourceSupplier {

    private final HikariDataSource source;

    public PersistenceSource(@Qualifier("persistenceDataSource") DataSource persistenceDataSource) {
        if (!(persistenceDataSource instanceof HikariDataSource)) {
            throw new IllegalArgumentException("Expected HikariDataSource for persistence");
        }
        
        this.source = (HikariDataSource) persistenceDataSource;
        applyMigration();
    }

    private void applyMigration() {
        Flyway.configure()
                .dataSource(source)
                .schemas("bc")
                .locations("bc_database_scripts")
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
