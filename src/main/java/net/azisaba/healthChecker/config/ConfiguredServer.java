package net.azisaba.healthChecker.config;

import net.azisaba.healthChecker.util.InvalidArgumentException;
import net.azisaba.healthChecker.util.StringReader;
import net.azisaba.healthChecker.yaml.YamlObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Locale;

public class ConfiguredServer {
    private final String name;
    private final Protocol protocol;
    private final InetSocketAddress host;
    private final int period;
    private final int threshold;
    private final String discordWebhook;
    private final String webhookMessagePrefix;

    public ConfiguredServer(@NotNull String name,
                            @NotNull Protocol protocol,
                            @NotNull InetSocketAddress host,
                            int period,
                            int threshold,
                            @Nullable String discordWebhook,
                            @Nullable String webhookMessagePrefix) {
        this.name = name;
        this.protocol = protocol;
        this.host = host;
        this.period = period;
        this.threshold = threshold;
        this.discordWebhook = discordWebhook;
        this.webhookMessagePrefix = webhookMessagePrefix;
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
        return new ConfiguredServer(name, protocol, explode(host), period, threshold, discordWebhook, webhookMessagePrefix);
    }

    @NotNull
    public static InetSocketAddress explode(@NotNull String s) throws InvalidArgumentException {
        StringReader reader = new StringReader(s);
        if (s.indexOf(':') == -1)
            throw InvalidArgumentException.createUnexpectedEOF(':').withContext(reader, reader.length(), 1);
        if (s.indexOf(':') != s.lastIndexOf(':')) {
            int idx = s.indexOf(':');
            while (!reader.isEOF()) {
                if (reader.peek() == ':' && reader.getIndex() != idx) {
                    break;
                }
                reader.readFirst();
            }
            throw new InvalidArgumentException("Malformed host:port string").withContext(reader, 0, reader.length() - reader.getIndex());
        }
        int port;
        try {
            port = Integer.parseInt(s.split(":")[1]);
            if (port <= 0 || port > 65535) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException("Invalid port").withContext(reader, s.indexOf(':'), s.length() - s.indexOf(':'));
        }
        InetAddress address;
        try {
            address = InetAddress.getByName(s.split(":")[0]);
        } catch (UnknownHostException e) {
            throw new InvalidArgumentException("Unknown host", e).withContext(reader, 0, s.indexOf(':'));
        }
        return new InetSocketAddress(address, port);
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
}
