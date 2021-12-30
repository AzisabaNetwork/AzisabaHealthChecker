package net.azisaba.healthChecker.util;

import net.azisaba.healthChecker.config.ConfiguredServer;
import net.azisaba.healthChecker.config.Protocol;
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

public class Util {
    public static void sendDiscordWebhook(@NotNull String url, @Nullable String username, @NotNull String content) throws IOException {
        if (url.isEmpty()) return;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
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
        connection.getInputStream().close();
        connection.disconnect();
    }

    public static void check(@NotNull ConfiguredServer server) throws IOException {
        if (server.getProtocol() == Protocol.TCP) {
            Socket socket = new Socket();
            socket.connect(server.getHost(), 1000);
            socket.close();
        } else {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(new byte[0], 0, server.getHost());
            socket.connect(server.getHost());
            socket.send(packet);
            packet = new DatagramPacket(new byte[1], 1);
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

    public static String timeToString(long timeInMillis) {
        StringBuilder sb = new StringBuilder();
        long days = timeInMillis / 1024 / 60 / 60 / 24;
        long hours = timeInMillis / 1024 / 60 / 60 % 24;
        long minutes = timeInMillis / 1000 / 60 % 60;
        long seconds = timeInMillis / 1000 % 60;
        long millis = timeInMillis % 1000;
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
        if (millis >= 1) {
            sb.append(millis).append(" milliseconds ");
        }
        return sb.toString().trim();
    }
}
