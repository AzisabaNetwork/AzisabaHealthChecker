package net.azisaba.healthChecker;

import net.azisaba.healthChecker.config.AppConfig;
import net.azisaba.healthChecker.config.CacheFile;
import net.azisaba.healthChecker.config.CachedData;
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
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final AtomicBoolean reallyUp = new AtomicBoolean(false);
    private Exception lastException = null;
    private boolean wasDown = false;

    public HealthCheckerTask(@NotNull ConfiguredServer server) {
        this.server = new Server(server);
        CachedData data = CacheFile.map.get(server.getName());
        if (data != null) {
            this.wasDown = data.wasDown;
            this.server.downSince = data.downSince;
        }
    }

    @Override
    public void run() {
        EXECUTOR.execute(() -> {
            if (!finished.get()) {
                if (server.downSince == 0) {
                    server.downSince = System.currentTimeMillis();
                }
                reallyUp.set(false);
            } else {
                if (reallyUp.get() && wasDown) {
                    String time = Util.timeToString(System.currentTimeMillis() - server.downSince);
                    LOGGER.info("{} ({}) is up. It was down for {}.", server.getConfig().getName(), server.getConfig().getHost(), time);
                    String url = server.getConfig().getEffectiveDiscordWebhook();
                    if (url != null) {
                        String prefix = Util.orElse(server.getConfig().getWebhookMessagePrefix(), "");
                        Util.sendDiscordWebhook(url, null, () -> prefix + ":o: " + server.getConfig().getName() + " is **up**. It was down for " + time + ".");
                    }

                    // save cache
                    wasDown = false;
                    CachedData data = CacheFile.map.computeIfAbsent(server.getConfig().getName(), name -> new CachedData(wasDown, server.downSince));
                    data.wasDown = false;
                    data.downSince = 0;
                    CacheFile.save();
                }
                if (!wasDown) {
                    server.downSince = 0;
                }
                lastException = null;
                reallyUp.set(true);
            }
            if (!wasDown && server.downSince > 0 && server.downSince <= System.currentTimeMillis() - (long) server.getConfig().getPeriod() * server.getConfig().getThreshold()) {
                wasDown = true;

                // save cache
                CachedData data = CacheFile.map.computeIfAbsent(server.getConfig().getName(), name -> new CachedData(wasDown, server.downSince));
                data.wasDown = true;
                data.downSince = server.downSince;
                CacheFile.save();

                String suffix = "";
                if (lastException != null) suffix = " (" + lastException.getClass().getSimpleName() + ": " + lastException.getMessage() + ")";
                LOGGER.info("{} ({}) is down since {} ago{}", server.getConfig().getName(), server.getConfig().getHost(), Util.timeToString(System.currentTimeMillis() - server.downSince), suffix);
                String url = server.getConfig().getEffectiveDiscordWebhook();
                if (url != null) {
                    String prefix = Util.orElse(server.getConfig().getWebhookMessagePrefix(), "");
                    String finalSuffix = suffix;
                    Util.sendDiscordWebhook(url, null, () -> prefix + ":x: " + server.getConfig().getName() + " is **down** since " + (Util.timeToString(System.currentTimeMillis() - server.downSince)) + " ago" + finalSuffix);
                }
            }
            finished.set(false);
            long start = System.currentTimeMillis();
            try {
                Util.check(server.getConfig());
            } catch (IOException e) {
                if (AppConfig.debug) LOGGER.warn("Error checking host {}", server.getConfig().getHost(), e);
                lastException = e;
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
