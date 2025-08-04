package club.ss220.manager.model;

import lombok.Data;
import lombok.Getter;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public abstract class PlayerExperience {

    private final TreeMap<RoleCategory, Duration> exp;

    public PlayerExperience(Map<RoleCategory, Duration> exp) {
        this.exp = fillExp(exp);
    }

    public abstract List<RoleCategory> getRoles();

    public Optional<Duration> getForRole(RoleCategory role) {
        return Optional.ofNullable(exp.get(role));
    }

    public TreeMap<RoleCategory, Duration> getAll() {
        return new TreeMap<>(exp);
    }

    private TreeMap<RoleCategory, Duration> fillExp(Map<RoleCategory, Duration> exp) {
        return getRoles().stream()
                .map(role -> Map.entry(role, exp.getOrDefault(role, Duration.ZERO)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Duration::plus,
                        TreeMap::new
                ));
    }
}
