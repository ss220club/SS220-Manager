package club.ss220.manager.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = BotCommandsDataSourceConfig.PACKAGE,
                       entityManagerFactoryRef = BotCommandsDataSourceConfig.EMF_REF,
                       transactionManagerRef = BotCommandsDataSourceConfig.TX_MANAGER_REF)
public class BotCommandsDataSourceConfig {

    public static final String UNIT_NAME = "bc";
    public static final String PROPERTIES_PREFIX = "spring.datasource." + UNIT_NAME;
    public static final String EMF_REF = UNIT_NAME + "EMF";
    public static final String TX_MANAGER_REF = UNIT_NAME + "TxManager";
    public static final String PACKAGE = "io.github.freya022.botcommands.api.core";

    @Bean
    @ConfigurationProperties(PROPERTIES_PREFIX)
    public DataSourceProperties bcDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @FlywayDataSource
    public DataSource bcDataSource() {
        return bcDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean bcEMF(EntityManagerFactoryBuilder builder) {
        return builder.dataSource(bcDataSource()).packages(PACKAGE).build();
    }

    @Bean
    public PlatformTransactionManager bcTxManager(@Qualifier(EMF_REF) EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
