package net.azisaba.healthchecker;

import net.azisaba.healthchecker.cloudflare.CFListDNSRecords;
import net.azisaba.healthchecker.cloudflare.CloudflareDNSRecord;
import net.azisaba.healthchecker.config.AppConfig;
import net.azisaba.healthchecker.config.CacheFile;
import net.azisaba.healthchecker.config.ConfiguredServer;
import net.azisaba.healthchecker.config.ZoneConfig;
import net.azisaba.healthchecker.server.Server;
import net.azisaba.healthchecker.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class AzisabaHealthChecker {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Timer timer = new Timer("Health Checker");
    private final List<Server> servers = new ArrayList<>();

    public void start() throws IOException {
        LOGGER.info("Loading config");
        AppConfig.init();
        CacheFile.load();
        scheduleTasks();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            timer.cancel();
            Util.TIMER.cancel();
            HealthCheckerTask.shutdown();
        }));
        LOGGER.info("All done!");
    }

    public void scheduleTasks() {
        for (ConfiguredServer server : AppConfig.servers) {
            LOGGER.info("========== Target '{}' ==========", server.getName());
            LOGGER.info("  Protocol: {}", server.getProtocol());
            LOGGER.info("  Host: {}", server.getHost());
            LOGGER.info("  Check every: {} ms", server.getPeriod());
            LOGGER.info("  Threshold: {}", server.getThreshold());
            LOGGER.info("  Discord Webhook: {}", server.getDiscordWebhook());
            LOGGER.info("  Effective Discord Webhook: {}", server.getEffectiveDiscordWebhook());
            LOGGER.info("  Webhook Message Prefix: '{}'", server.getWebhookMessagePrefix());
            LOGGER.info("  Zone ID: {}", server.getZoneId());
            HealthCheckerTask task = new HealthCheckerTask(server);
            timer.scheduleAtFixedRate(task, 1000, server.getPeriod());
            servers.add(task.getServer());
        }
        for (ZoneConfig zone : AppConfig.zones) {
            List<CloudflareDNSRecord> records;
            try {
                records = new CFListDNSRecords(zone.getEffectiveCloudflareEndpoint(), zone.getApiToken(), zone.getId()).execute();
            } catch (Exception e) {
                LOGGER.error("Error listing DNS records in zone {}: {}", zone.getId(), e.getMessage());
                LOGGER.error("Skipping zone {} due to the error above", zone.getId());
                continue;
            }
            Map<String, CloudflareDNSRecord> dnsRecords = new HashMap<>();
            List<String> lowercasedNames = zone.getRecordNames().stream().map(String::toLowerCase).collect(Collectors.toList());
            for (CloudflareDNSRecord record : records) {
                String lowercaseName = record.getName().toLowerCase(Locale.ROOT);
                if (dnsRecords.containsKey(lowercaseName) && lowercasedNames.contains(lowercaseName)) {
                    LOGGER.warn("More than one record with the same name {} in zone {} found! This is unsupported and might not work properly. Overwriting existing DNS with type {}", record.getName(), zone.getId(), record.getType());
                }
                dnsRecords.put(lowercaseName, record);
            }
            LOGGER.info("========== Zone {} ==========", zone.getId());
            LOGGER.info("  API Token: (hidden)");
            LOGGER.info("  API Endpoint: {}", zone.getEffectiveCloudflareEndpoint());
            LOGGER.info("  Update every: {} ms", zone.getUpdateEvery());
            LOGGER.info("  Record names:");
            for (String name : zone.getRecordNames()) {
                LOGGER.info("  - {}", name);
            }
            if (!zone.getRecordNames().isEmpty()) {
                timer.scheduleAtFixedRate(new ZoneUpdateTask(servers, zone, dnsRecords), zone.getUpdateEvery(), zone.getUpdateEvery());
            }
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                CacheFile.save();
            }
        }, 1000 * 30, 1000 * 30);
    }
}
