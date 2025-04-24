package net.azisaba.healthchecker.cloudflare;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class CFPatchDNSRecord extends CloudflareRequest<JSONObject> {
    public CFPatchDNSRecord(@NotNull String endpoint, @NotNull String token, @NotNull String zone, @NotNull String id, @NotNull String content, int ttl) {
        super(endpoint,
                token,
                "/zones/" + zone + "/dns_records/" + id,
                "PATCH",
                CloudflareRequest.buildJson("content", content, "ttl", ttl).toString());
    }

    @Override
    protected JSONObject parse(@NotNull JSONObject json) {
        return json;
    }
}
