package club.ss220.manager.data.integration.game.impl;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;
import java.util.Map;

public class ServerResponse {

    protected final Map<String, Object> data;

    public ServerResponse() {
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
}
