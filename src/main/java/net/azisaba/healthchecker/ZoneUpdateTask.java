package net.azisaba.healthchecker;

import net.azisaba.healthchecker.cloudflare.CFPatchDNSRecord;
import net.azisaba.healthchecker.cloudflare.CFPatchSRVDNSRecord;
import net.azisaba.healthchecker.cloudflare.CloudflareDNSRecord;
import net.azisaba.healthchecker.config.AppConfig;
import net.azisaba.healthchecker.config.ConfiguredServer;
import net.azisaba.healthchecker.config.ZoneConfig;
import net.azisaba.healthchecker.server.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class ZoneUpdateTask extends TimerTask {
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<Server> servers;
    private final ZoneConfig zone;
    private final Map<String, CloudflareDNSRecord> dnsTypes;

    public ZoneUpdateTask(@NotNull List<Server> servers, @NotNull ZoneConfig zone, @NotNull Map<String, CloudflareDNSRecord> dnsTypes) {
        this.servers = servers.stream().filter(server -> zone.getId().equals(server.getConfig().getZoneId())).collect(Collectors.toList());
        this.zone = zone;
        this.dnsTypes = dnsTypes;
    }

    @Override
    public void run() {
        List<Server> onlineServers = servers.stream().filter(Server::isUp).collect(Collectors.toList());
        if (onlineServers.isEmpty()) {
            if (AppConfig.debug) {
                LOGGER.warn("No online servers in zone {}. Skipping update.", zone.getId());
            }
            return;
        }
        Server selectedServer = zone.getStrategy().select(onlineServers);
        ConfiguredServer serverConfig = selectedServer.getConfig();
        for (String recordName : zone.getRecordNames()) {
            CloudflareDNSRecord record = dnsTypes.get(recordName.toLowerCase());
            if (record == null) {
                LOGGER.warn("No DNS record found for {} in zone {}. Skipping update.", recordName, zone.getId());
                continue;
            }
            try {
                if (record.getType().equals("SRV")) {
                    new CFPatchSRVDNSRecord(
                            zone.getEffectiveCloudflareEndpoint(),
                            zone.getApiToken(),
                            zone.getId(),
                            record.getId(),
                            "0\t" + serverConfig.getHost().getHostString() + "\t" + serverConfig.getHost().getPort(),
                            zone.getPort(),
                            zone.getTtl()
                    ).execute();
                    if (AppConfig.debug) {
                        LOGGER.info("Updated SRV record {} in zone {} to {}:{}", recordName, zone.getId(), serverConfig.getHost().getHostString(), serverConfig.getHost().getPort());
                    }
                } else {
                    new CFPatchDNSRecord(
                            zone.getEffectiveCloudflareEndpoint(),
                            zone.getApiToken(),
                            zone.getId(),
                            record.getId(),
                            serverConfig.getHost().getAddress().getHostAddress(),
                            zone.getTtl()
                    ).execute();
                    if (AppConfig.debug) {
                        LOGGER.info("Updated {} record {} in zone {} to {}", record.getType(), recordName, zone.getId(), serverConfig.getHost().getAddress().getHostAddress());
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Error updating DNS record {} in zone {}: {}", recordName, zone.getId(), e.getMessage());
            }
        }
    }
}
