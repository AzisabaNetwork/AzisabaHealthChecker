package net.azisaba.healthChecker;

import net.azisaba.healthChecker.config.AppConfig;
import net.azisaba.healthChecker.config.ConfiguredServer;
import net.azisaba.healthChecker.server.Server;
import net.azisaba.healthChecker.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class HealthCheckerTask extends TimerTask {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final AtomicInteger ID = new AtomicInteger();
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "Health Checker Worker #" + ID.incrementAndGet());
        t.setDaemon(true);
        return t;
    });
    private final Server server;
    private final AtomicBoolean finished = new AtomicBoolean(true);

    public HealthCheckerTask(@NotNull ConfiguredServer server) {
        this.server = new Server(server);
    }

    @Override
    public void run() {
        EXECUTOR.execute(() -> {
            if (!finished.get()) {
                server.failCount++;
            } else {
                if (server.failCount > server.getConfig().getThreshold()) {
                    LOGGER.info("{} ({}) is up. It was down for {}. (Attempted {} times)", server.getConfig().getName(), server.getConfig().getHost(), Util.timeToString((long) server.failCount * server.getConfig().getPeriod()), server.failCount);
                    String url = server.getConfig().getEffectiveDiscordWebhook();
                    if (url != null) {
                        try {
                            String prefix = server.getConfig().getWebhookMessagePrefix();
                            if (prefix == null) prefix = "";
                            Util.sendDiscordWebhook(url, null, prefix + ":o: " + server.getConfig().getName() + " is **up**. It was down for " + Util.timeToString((long) server.failCount * server.getConfig().getPeriod()) + ". (Attempted " + server.failCount + " times)");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                server.failCount = 0;
            }
            if (server.failCount == server.getConfig().getThreshold()) {
                LOGGER.info("{} ({}) is down after {} tries ({})", server.getConfig().getName(), server.getConfig().getHost(), server.failCount, Util.timeToString((long) server.failCount * server.getConfig().getPeriod()));
                String url = server.getConfig().getEffectiveDiscordWebhook();
                if (url != null) {
                    try {
                        String prefix = server.getConfig().getWebhookMessagePrefix();
                        if (prefix == null) prefix = "";
                        Util.sendDiscordWebhook(url, null, prefix + ":x: " + server.getConfig().getName() + " is **down**");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            finished.set(false);
            long start = System.currentTimeMillis();
            try {
                Util.check(server.getConfig());
            } catch (IOException e) {
                if (AppConfig.debug) LOGGER.warn("Error checking host {}", server.getConfig().getHost(), e);
                return;
            }
            long total = System.currentTimeMillis() - start;
            if (total > server.getConfig().getPeriod()) return;
            finished.set(true);
        });
    }

    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }
}
