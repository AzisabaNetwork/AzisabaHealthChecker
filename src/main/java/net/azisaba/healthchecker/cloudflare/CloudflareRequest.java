package net.azisaba.healthchecker.cloudflare;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

public abstract class CloudflareRequest<R> {
    private final @NotNull String endpoint;
    private final @NotNull String token;
    private final @NotNull String path;
    private final @NotNull String method;
    private final @Nullable String body;

    public CloudflareRequest(@NotNull String endpoint, @NotNull String token, @NotNull String path, @NotNull String method, @Nullable String body) {
        this.endpoint = endpoint;
        this.token = token;
        this.path = path;
        this.method = method;
        this.body = body;
    }

    public @NotNull String getEndpoint() {
        return endpoint;
    }

    public @NotNull String getPath() {
        return path;
    }

    public @NotNull String getFullUrl() {
        return getEndpoint() + getPath();
    }

    public @NotNull String getMethod() {
        return method;
    }

    public @Nullable String getBody() {
        return body;
    }

    protected final @NotNull String call() throws IOException, URISyntaxException {
        HttpURLConnection connection = (HttpURLConnection) new URI(getFullUrl()).toURL().openConnection();
        connection.setRequestMethod(getMethod());
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        if (getBody() != null) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.getOutputStream().write(getBody().getBytes());
        }
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            StringBuilder response = new StringBuilder();
            try (Scanner s = new Scanner(connection.getErrorStream())) {
                while (s.hasNextLine()) {
                    response.append(s.nextLine());
                }
            }
            throw new IOException("Unexpected response code: " + responseCode + " " + connection.getResponseMessage() + ", received body: " + response);
        }
        StringBuilder response = new StringBuilder();
        try (Scanner s = new Scanner(connection.getInputStream())) {
            while (s.hasNextLine()) {
                response.append(s.nextLine());
            }
        }
        return response.toString();
    }

    protected final @NotNull JSONObject callJson() throws IOException, URISyntaxException {
        return new JSONObject(call());
    }

    protected abstract R parse(@NotNull JSONObject json);

    public final R execute() {
        try {
            JSONObject response = callJson();
            if (response.getBoolean("success")) {
                return parse(response);
            } else {
                throw new IOException("Cloudflare API returned error: " + response.getJSONArray("errors").toString());
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    protected static @NotNull JSONObject buildJson(@NotNull Object... keysAndValues) {
        if (keysAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("keysAndValues must be an even number of arguments");
        }
        JSONObject json = new JSONObject();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            json.put(keysAndValues[i].toString(), keysAndValues[i + 1]);
        }
        return json;
    }
}
