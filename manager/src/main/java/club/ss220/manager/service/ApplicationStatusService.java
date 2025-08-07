package club.ss220.manager.service;

import club.ss220.manager.model.ApplicationStatus;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class ApplicationStatusService {

    private final Environment environment;
    private final DataSource bcDataSource;
    private final String revision;

    private final Instant startTime = Instant.now();

    public ApplicationStatusService(Environment environment, DataSource bcDataSource,
                                    @Value("${spring.application.revision}") String revision) {
        this.environment = environment;
        this.bcDataSource = bcDataSource;
        this.revision = revision;
    }

    public ApplicationStatus getApplicationStatus(Guild guild) {
        JDA jda = guild.getJDA();
        Duration uptime = Duration.between(startTime, Instant.now());
        List<String> profiles = List.of(environment.getActiveProfiles());
        long discordLatency = jda.getGatewayPing();
        int globalCommands = jda.retrieveCommands().complete().size();
        int guildCommands = guild.retrieveCommands().complete().size();
        boolean persistenceStatus = getPersistenceStatus();

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        String javaVersion = System.getProperty("java.version");
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        int threadCount = threadBean.getThreadCount();

        return new ApplicationStatus(
                revision,
                uptime,
                profiles,
                discordLatency,
                globalCommands,
                guildCommands,
                persistenceStatus,
                javaVersion,
                threadCount,
                heapUsed,
                heapMax
        );
    }

    private boolean getPersistenceStatus() {
        try (Connection connection = bcDataSource.getConnection()) {
            int timeout = 5;
            return connection.isValid(timeout);
        } catch (Exception e) {
            return false;
        }
    }
}
