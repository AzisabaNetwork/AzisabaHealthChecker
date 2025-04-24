package net.azisaba.healthchecker.server;

import net.azisaba.healthchecker.HealthCheckerTask;
import net.azisaba.healthchecker.config.ConfiguredServer;
import org.jetbrains.annotations.NotNull;

public class Server {
    private final HealthCheckerTask task;
    private final ConfiguredServer config;
    public long downSince = 0;

    public Server(@NotNull HealthCheckerTask task, @NotNull ConfiguredServer config) {
        this.task = task;
        this.config = config;
    }

    public @NotNull HealthCheckerTask getTask() {
        return task;
    }

    public boolean isUp() {
        return task.isUp();
    }

    @NotNull
    public ConfiguredServer getConfig() {
        return config;
    }
}
