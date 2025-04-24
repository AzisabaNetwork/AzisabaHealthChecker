package net.azisaba.healthchecker.cloudflare;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class CFPatchSRVDNSRecord extends CloudflareRequest<JSONObject> {
    public CFPatchSRVDNSRecord(@NotNull String endpoint, @NotNull String token, @NotNull String zone, @NotNull String id, @NotNull String target, int port, int ttl) {
        super(endpoint,
                token,
                "/zones/" + zone + "/dns_records/" + id,
                "PATCH",
                CloudflareRequest.buildJson("data", CloudflareRequest.buildJson("port", port, "target", target), "ttl", ttl).toString());
    }

    @Override
    protected JSONObject parse(@NotNull JSONObject json) {
        return json;
    }
}
