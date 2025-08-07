package club.ss220.core.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = BandastationDataSourceConfig.PACKAGE,
                       entityManagerFactoryRef = BandastationDataSourceConfig.EMF_REF,
                       transactionManagerRef = BandastationDataSourceConfig.TX_MANAGER_REF)
public class BandastationDataSourceConfig {

    public static final String UNIT_NAME = "bandastation";
    public static final String PROPERTIES_PREFIX = "spring.datasource." + UNIT_NAME;
    public static final String EMF_REF = UNIT_NAME + "EMF";
    public static final String TX_MANAGER_REF = UNIT_NAME + "TxManager";
    public static final String PACKAGE = "club.ss220.core.data.db." + UNIT_NAME;

    @Bean
    @ConfigurationProperties(PROPERTIES_PREFIX)
    public DataSourceProperties bandastationDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource bandastationDataSource() {
        return bandastationDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean bandastationEMF(EntityManagerFactoryBuilder builder) {
        return builder.dataSource(bandastationDataSource()).packages(PACKAGE).build();
    }

    @Bean
    public PlatformTransactionManager bandastationTxManager(@Qualifier(EMF_REF) EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
