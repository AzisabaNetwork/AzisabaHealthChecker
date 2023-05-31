package net.azisaba.healthchecker.util;

import net.azisaba.healthchecker.config.ConfiguredServer;
import net.azisaba.healthchecker.config.Protocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

public class Util {
    private static final Logger LOGGER = LogManager.getLogger();
    private static long discordRateLimitReset = 0;
    private static final List<Tuple<String, String, Supplier<String>>> WEBHOOK_QUEUE = Collections.synchronizedList(new ArrayList<>());
    public static final Timer TIMER = new Timer("WebhookExecutor");

    static {
        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                if (discordRateLimitReset > System.currentTimeMillis()) return;
                if (WEBHOOK_QUEUE.isEmpty()) return;
                Tuple<String, String, Supplier<String>> tuple = WEBHOOK_QUEUE.remove(0);
                if (tuple == null) return;
                try {
                    doSendDiscordWebhook(tuple.getFirst(), tuple.getSecond(), tuple.getThird());
                } catch (IOException e) {
                    LOGGER.warn("Failed to execute Discord webhook", e);
                }
            }
        }, 100, 100);
    }

    public static void sendDiscordWebhook(@NotNull String url, @Nullable String username, @NotNull Supplier<@Nullable String> contentSupplier) {
        if (url.isEmpty()) return;
        WEBHOOK_QUEUE.add(new Tuple<>(url, username, contentSupplier));
    }

    private static void doSendDiscordWebhook(@NotNull String url, @Nullable String username, @NotNull Supplier<@Nullable String> contentSupplier) throws IOException {
        String content = contentSupplier.get();
        if (content == null || content.isEmpty()) return;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        try {
            connection.addRequestProperty("Accept", "application/json");
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("User-Agent", "AzisabaHealthChecker/1.0.0");
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            OutputStream stream = connection.getOutputStream();
            JSONObject json = new JSONObject();
            if (username != null) {
                json.put("username", username);
            }
            json.put("content", content);
            stream.write(json.toString().getBytes(StandardCharsets.UTF_8));
            stream.flush();
            stream.close();
            connection.connect();
            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                LOGGER.warn("Server returned non-2xx HTTP response code: {}", code);
                WEBHOOK_QUEUE.add(new Tuple<>(url, username, contentSupplier));
            }
            long retryAfter = (long) Math.ceil(Optional.ofNullable(connection.getHeaderField("Retry-After")).map(Double::parseDouble).orElse(0.0));
            if (retryAfter > 0) {
                discordRateLimitReset = System.currentTimeMillis() + retryAfter + 1000;
                LOGGER.warn("Waiting until {} (Received Retry-After: {})", discordRateLimitReset, connection.getHeaderField("Retry-After"));
                return;
            }
            int remaining = connection.getHeaderFieldInt("X-RateLimit-Remaining", 0);
            if (remaining == 0) {
                discordRateLimitReset = (long) (Double.parseDouble(connection.getHeaderField("X-RateLimit-Reset")) * 1000 + 1000);
                //LOGGER.info("Rate limit reset: {} (Raw: {})", discordRateLimitReset, connection.getHeaderField("X-RateLimit-Reset"));
            }
        } finally {
            connection.disconnect();
        }
    }

    public static void check(@NotNull ConfiguredServer server) throws IOException {
        if (server.getProtocol() == Protocol.TCP) {
            Socket socket = new Socket();
            socket.connect(server.getHost(), server.getPeriod());
            socket.close();
        } else {
            DatagramSocket socket = new DatagramSocket();
            new Thread(() -> {
                try {
                    Thread.sleep(server.getPeriod());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    socket.close();
                }
            }).start();
            DatagramPacket packet = new DatagramPacket(new byte[] {0}, 1, server.getHost());
            socket.send(packet);
            packet = new DatagramPacket(new byte[0], 0);
            socket.receive(packet);
            socket.close();
        }
    }

    /*
    public static long check(@NotNull ConfiguredServer server) throws IOException {
        if (server.getProtocol() == Protocol.TCP) {
            Socket socket = new Socket();
            socket.connect(server.getHost(), 1000);
            socket.getOutputStream().write(new byte[0]);
            socket.getOutputStream().flush();
            byte[] bytes = new byte[8];
            int read = socket.getInputStream().read(bytes);
            long l = -1;
            if (read == 8) l = readLong(bytes, 0);
            socket.close();
            return l;
        } else {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(new byte[0], 0, server.getHost());
            socket.connect(server.getHost());
            socket.send(packet);
            packet = new DatagramPacket(new byte[8], 8);
            socket.receive(packet);
            socket.close();
            return readLong(packet.getData(), 0);
        }
    }

    public static long readLong(byte[] bytes, int index) {
        return  ((long) bytes[index    ] & 0xff) << 56 |
                ((long) bytes[index + 1] & 0xff) << 48 |
                ((long) bytes[index + 2] & 0xff) << 40 |
                ((long) bytes[index + 3] & 0xff) << 32 |
                ((long) bytes[index + 4] & 0xff) << 24 |
                ((long) bytes[index + 5] & 0xff) << 16 |
                ((long) bytes[index + 6] & 0xff) <<  8 |
                ((long) bytes[index + 7] & 0xff);
    }
    */

    @NotNull
    public static String timeToString(long timeInMillis) {
        if (timeInMillis == 0) return "0 seconds";
        StringBuilder sb = new StringBuilder();
        long days = timeInMillis / 1024 / 60 / 60 / 24;
        long hours = timeInMillis / 1024 / 60 / 60 % 24;
        long minutes = timeInMillis / 1000 / 60 % 60;
        long seconds = timeInMillis / 1000 % 60;
        //long millis = timeInMillis % 1000;
        if (days >= 1) {
            sb.append(days).append(" days ");
        }
        if (hours >= 1) {
            sb.append(hours).append(" hours ");
        }
        if (minutes >= 1) {
            sb.append(minutes).append(" minutes ");
        }
        if (seconds >= 1) {
            sb.append(seconds).append(" seconds ");
        }
        //if (millis >= 1) {
        //    sb.append(millis).append(" milliseconds ");
        //}
        return sb.toString().trim();
    }

    public static <T> T orElse(T value, T def) {
        if (value == null) return def;
        return value;
    }
}
