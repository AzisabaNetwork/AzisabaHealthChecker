package net.azisaba.healthChecker.server;

import net.azisaba.healthChecker.config.ConfiguredServer;
import org.jetbrains.annotations.NotNull;

public class Server {
    private final ConfiguredServer config;
    public long downSince = 0;

    public Server(ConfiguredServer config) {
        this.config = config;
    }

    @NotNull
    public ConfiguredServer getConfig() {
        return config;
    }
}
