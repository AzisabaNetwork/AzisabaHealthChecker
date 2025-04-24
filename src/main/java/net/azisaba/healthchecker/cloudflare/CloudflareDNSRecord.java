package net.azisaba.healthchecker.cloudflare;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class CloudflareDNSRecord {
    private final String id;
    private final String type;
    private final String name;
    private final String content;
    private final boolean proxiable;
    private final boolean proxied;
    private final int ttl;

    public CloudflareDNSRecord(String id, String type, String name, String content, boolean proxiable, boolean proxied, int ttl) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.content = content;
        this.proxiable = proxiable;
        this.proxied = proxied;
        this.ttl = ttl;
    }

    public static @NotNull CloudflareDNSRecord parse(@NotNull JSONObject json) {
        String id = json.getString("id");
        String type = json.getString("type");
        String name = json.getString("name");
        String content = json.getString("content");
        boolean proxiable = json.getBoolean("proxiable");
        boolean proxied = json.getBoolean("proxied");
        int ttl = json.getInt("ttl");
        return new CloudflareDNSRecord(id, type, name, content, proxiable, proxied, ttl);
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public boolean isProxiable() {
        return proxiable;
    }

    public boolean isProxied() {
        return proxied;
    }

    public int getTtl() {
        return ttl;
    }
}
