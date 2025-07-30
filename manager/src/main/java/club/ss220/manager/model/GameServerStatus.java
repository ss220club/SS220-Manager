package club.ss220.manager.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public abstract class GameServerStatus {

    protected final Map<String, Object> data;

    public GameServerStatus() {
        this.data = new HashMap<>();
    }

    @JsonAnyGetter
    public Map<String, Object> getData() {
        return data;
    }

    @JsonAnySetter
    public void setData(String key, Object value) {
        data.put(key, value);
    }

    public abstract Integer getPlayers();

    public abstract Integer getAdmins();

    public abstract Duration getRoundDuration();
}
