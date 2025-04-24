package net.azisaba.healthchecker.cloudflare;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CFListDNSRecords extends CloudflareRequest<List<CloudflareDNSRecord>> {
    public CFListDNSRecords(@NotNull String endpoint, @NotNull String token, @NotNull String zone) {
        super(endpoint, token, "/zones/" + zone + "/dns_records?per_page=5000000", "GET", null);
    }

    @Override
    protected List<CloudflareDNSRecord> parse(@NotNull JSONObject json) {
        List<CloudflareDNSRecord> records = new ArrayList<>();
        for (Object o : json.getJSONArray("result")) {
            if (!(o instanceof JSONObject)) continue;
            JSONObject record = (JSONObject) o;
            records.add(CloudflareDNSRecord.parse(record));
        }
        return Collections.unmodifiableList(records);
    }
}
