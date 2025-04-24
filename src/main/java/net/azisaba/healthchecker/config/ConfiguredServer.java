package net.azisaba.healthchecker.config;

import net.azisaba.healthchecker.util.InvalidArgumentException;
import net.azisaba.healthchecker.util.StringReader;
import net.azisaba.healthchecker.util.Util;
import net.azisaba.healthchecker.yaml.YamlObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Locale;

public class ConfiguredServer {
    private final @NotNull String name;
    private final @NotNull Protocol protocol;
    private final @NotNull InetSocketAddress host;
    private final int period;
    private final int threshold;
    private final @Nullable String discordWebhook;
    private final @Nullable String webhookMessagePrefix;
    private final @Nullable String zoneId;

    public ConfiguredServer(@NotNull String name,
                            @NotNull Protocol protocol,
                            @NotNull InetSocketAddress host,
                            int period,
                            int threshold,
                            @Nullable String discordWebhook,
                            @Nullable String webhookMessagePrefix,
                            @Nullable String zoneId) {
        this.name = name;
        this.protocol = protocol;
        this.host = host;
        this.period = period;
        this.threshold = threshold;
        this.discordWebhook = discordWebhook;
        this.webhookMessagePrefix = webhookMessagePrefix;
        this.zoneId = zoneId;
    }

    @NotNull
    public static ConfiguredServer read(@NotNull YamlObject obj) throws InvalidArgumentException {
        String name = obj.getString("name");
        if (name == null) throw new InvalidArgumentException("name is null");
        String protocolString = obj.getString("protocol", "TCP");
        Protocol protocol;
        try {
            protocol = Protocol.valueOf(protocolString.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidArgumentException("Invalid protocol").withContext(new StringReader("protocol: " + protocolString), 10, protocolString.length());
        }
        String host = obj.getString("host");
        if (host == null) throw new InvalidArgumentException("host is null");
        int period = obj.getInt("period", 5000); // defaults to 5 seconds
        int threshold = obj.getInt("threshold", 10);
        String discordWebhook = obj.getString("discordWebhook");
        String webhookMessagePrefix = obj.getString("webhookMessagePrefix");
        String zoneId = obj.getString("zoneId");
        return new ConfiguredServer(name, protocol, Util.explode(host), period, threshold, discordWebhook, webhookMessagePrefix, zoneId);
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Protocol getProtocol() {
        return protocol;
    }

    @NotNull
    public InetSocketAddress getHost() {
        return host;
    }

    public int getPeriod() {
        return period;
    }

    public int getThreshold() {
        return threshold;
    }

    @Nullable
    public String getDiscordWebhook() {
        return discordWebhook;
    }

    @Nullable
    public String getEffectiveDiscordWebhook() {
        if (getDiscordWebhook() != null) return getDiscordWebhook();
        return AppConfig.discordWebhook;
    }

    @Nullable
    public String getWebhookMessagePrefix() {
        return webhookMessagePrefix;
    }

    public @Nullable String getZoneId() {
        return zoneId;
    }
}
