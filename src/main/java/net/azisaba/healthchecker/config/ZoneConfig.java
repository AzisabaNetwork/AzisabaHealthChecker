package net.azisaba.healthchecker.config;

import net.azisaba.healthchecker.util.InvalidArgumentException;
import net.azisaba.healthchecker.yaml.YamlArray;
import net.azisaba.healthchecker.yaml.YamlObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class ZoneConfig {
    private final @NotNull String id;
    private final @NotNull String apiToken;
    private final @Nullable String cloudflareEndpoint;
    private final int updateEvery;
    private final @NotNull List<@NotNull String> recordNames;
    private final @NotNull SelectionStrategy strategy;
    private final int ttl;
    private final int port;

    public ZoneConfig(
            @NotNull String id,
            @NotNull String apiToken,
            @Nullable String cloudflareEndpoint,
            int updateEvery,
            @NotNull List<String> recordNames,
            @NotNull SelectionStrategy strategy,
            int ttl,
            int port) {
        this.id = id;
        this.apiToken = apiToken;
        this.cloudflareEndpoint = cloudflareEndpoint;
        this.updateEvery = updateEvery;
        this.recordNames = recordNames;
        this.strategy = strategy;
        this.ttl = ttl;
        this.port = port;
    }

    @NotNull
    public static ZoneConfig read(@NotNull YamlObject obj) throws InvalidArgumentException {
        String id = obj.getString("id");
        if (id == null) throw new InvalidArgumentException("name is null");
        String apiToken = obj.getString("apiToken");
        if (apiToken == null) throw new InvalidArgumentException("apiToken is null");
        String cloudflareEndpoint = obj.getString("cloudflareEndpoint");
        int updateEvery = obj.getInt("updateEvery", 86400000);
        YamlArray rawRecordNames = obj.getArray("recordNames");
        if (rawRecordNames == null) throw new InvalidArgumentException("recordNames is null");
        List<String> recordNames = rawRecordNames.mapToString();
        String strategyString = obj.getString("strategy", "FIRST").toUpperCase(Locale.ROOT);
        SelectionStrategy strategy = SelectionStrategy.valueOf(strategyString);
        int ttl = obj.getInt("ttl", 120);
        int port = obj.getInt("port", 25565);
        return new ZoneConfig(id, apiToken, cloudflareEndpoint, updateEvery, recordNames, strategy, ttl, port);
    }

    public @NotNull String getId() {
        return id;
    }

    public @NotNull String getApiToken() {
        return apiToken;
    }

    public @Nullable String getCloudflareEndpoint() {
        return cloudflareEndpoint;
    }

    public @NotNull String getEffectiveCloudflareEndpoint() {
        if (getCloudflareEndpoint() != null) {
            String endpoint = getCloudflareEndpoint();
            if (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            return endpoint;
        }
        return "https://api.cloudflare.com/client/v4";
    }

    public int getUpdateEvery() {
        return updateEvery;
    }

    public @NotNull List<@NotNull String> getRecordNames() {
        return recordNames;
    }

    public @NotNull SelectionStrategy getStrategy() {
        return strategy;
    }

    public int getTtl() {
        return ttl;
    }

    public int getPort() {
        return port;
    }
}
