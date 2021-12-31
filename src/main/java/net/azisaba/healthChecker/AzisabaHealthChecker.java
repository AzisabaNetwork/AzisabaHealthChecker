package net.azisaba.healthChecker;

import net.azisaba.healthChecker.config.AppConfig;
import net.azisaba.healthChecker.config.CacheFile;
import net.azisaba.healthChecker.config.ConfiguredServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Timer;

public class AzisabaHealthChecker {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Timer timer = new Timer("Health Checker");

    public void start() throws IOException {
        LOGGER.info("Loading config");
        AppConfig.init();
        CacheFile.load();
        scheduleTasks();
        Runtime.getRuntime().addShutdownHook(new Thread(HealthCheckerTask::shutdown));
        LOGGER.info("All done!");
    }

    public void scheduleTasks() {
        for (ConfiguredServer server : AppConfig.servers) {
            LOGGER.info("========== {} ==========", server.getName());
            LOGGER.info("  Protocol: {}", server.getProtocol());
            LOGGER.info("  Host: {}", server.getHost());
            LOGGER.info("  Check every: {} ms", server.getPeriod());
            LOGGER.info("  Threshold: {}", server.getThreshold());
            LOGGER.info("  Discord Webhook: {}", server.getDiscordWebhook());
            LOGGER.info("  Effective Discord Webhook: {}", server.getEffectiveDiscordWebhook());
            LOGGER.info("  Webhook Message Prefix: '{}'", server.getWebhookMessagePrefix());
            timer.scheduleAtFixedRate(new HealthCheckerTask(server), server.getPeriod(), server.getPeriod());
        }
    }
}
