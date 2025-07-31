package club.ss220.manager.model;

import lombok.Getter;

import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public record ApplicationStatus(
        String revision,
        Duration uptime,
        List<String> profiles,
        long latency,
        int globalCommands,
        int guildCommands,
        boolean persistenceStatus,

        // JVS metrics
        String javaVersion,
        int threadCount,
        long heapUsed,
        long heapMax
) {

    public Level getSummaryLevel() {
        return Arrays.stream(Level.values())
                .filter(this::hasAnyMetricAtLevel)
                .max(Comparator.naturalOrder())
                .orElse(Level.OK);
    }

    private boolean hasAnyMetricAtLevel(Level level) {
        return getLatencyLevel().ordinal() >= level.ordinal()
               || getMemoryLevel().ordinal() >= level.ordinal()
               || getPersistenceLevel().ordinal() >= level.ordinal()
               || getThreadLevel().ordinal() >= level.ordinal();
    }

    public Level getLatencyLevel() {
        return mapLatency(latency);
    }

    public Level getMemoryLevel() {
        return mapMemoryUsage(heapUsed, heapMax);
    }

    public Level getPersistenceLevel() {
        return persistenceStatus ? Level.OK : Level.CRITICAL;
    }

    public Level getThreadLevel() {
        return mapThreadCount(threadCount);
    }

    private static Level mapLatency(Long latency) {
        if (latency == null) {
            return Level.CRITICAL;
        }
        if (latency <= 500) {
            return Level.OK;
        }
        if (latency <= 2000) {
            return Level.WARNING;
        }
        return Level.CRITICAL;
    }

    private static Level mapMemoryUsage(long heapUsed, long heapMax) {
        if (heapMax == 0) {
            return Level.CRITICAL;
        }

        double memoryUsagePercent = (double) heapUsed / heapMax * 100;
        if (memoryUsagePercent <= 80) {
            return Level.OK;
        }
        if (memoryUsagePercent <= 95) {
            return Level.WARNING;
        }
        return Level.CRITICAL;
    }

    private static Level mapThreadCount(int threadCount) {
        if (threadCount >= 10 && threadCount <= 200) {
            return Level.OK;
        }
        if (threadCount >= 5 && threadCount <= 300) {
            return Level.WARNING;
        }
        return Level.CRITICAL;
    }

    @Getter
    public enum Level {
        OK("Fully operational"),
        WARNING("Issues detected"),
        CRITICAL("Critical issues detected");

        private final String description;

        Level(String description) {
            this.description = description;
        }
    }
}
