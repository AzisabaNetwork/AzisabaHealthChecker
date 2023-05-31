package net.azisaba.healthchecker.config;

import net.azisaba.healthchecker.yaml.YamlArray;
import net.azisaba.healthchecker.yaml.YamlConfiguration;
import net.azisaba.healthchecker.yaml.YamlObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class AppConfig {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<Field> FIELDS = new ArrayList<>();
    private static YamlObject config;

    public static void init() throws IOException, ClassCastException {
        reset();
        File file = new File("./config.yml");
        boolean shouldSave = !file.exists();
        if (!file.exists() && !file.createNewFile()) {
            LOGGER.warn("Failed to create " + file.getAbsolutePath());
        }
        config = new YamlConfiguration(file).asObject();
        for (Field field : FIELDS) {
            String serializedName = field.getAnnotation(SerializedName.class).value();
            try {
                Object def = field.get(null);
                field.set(null, config.get(serializedName, def));
            } catch (ReflectiveOperationException ex) {
                LOGGER.warn("Failed to get or set field '{}' (serialized name: {})", field.getName(), serializedName, ex);
            }
        }
        YamlArray rules = config.getArray("servers");
        if (rules != null) {
            servers.read(rules);
        }
        if (shouldSave) save();
    }

    public static void save() throws IOException {
        if (config == null) throw new RuntimeException("#init was not called");
        for (Field field : FIELDS) {
            String serializedName = field.getAnnotation(SerializedName.class).value();
            try {
                Object value = field.get(null);
                config.setNullable(serializedName, value);
            } catch (ReflectiveOperationException ex) {
                LOGGER.warn("Failed to get field '{}' (serialized name: {})", field.getName(), serializedName, ex);
            }
        }
        config.save(new File("./config.yml"));
    }

    public static void reset() {
        servers.clear();
        debug = false;
        verbose = true;
        discordWebhook = null;
    }

    @NotNull
    public static ServerSet servers = new ServerSet();

    @SerializedName("debug")
    public static boolean debug = false;

    @SerializedName("verbose")
    public static boolean verbose = true;

    @Nullable
    @SerializedName("discordWebhook")
    public static String discordWebhook = null;

    static {
        for (Field field : AppConfig.class.getFields()) {
            if (!Modifier.isPublic(field.getModifiers())) continue;
            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (field.isSynthetic()) continue;
            SerializedName serializedNameAnnotation = field.getAnnotation(SerializedName.class);
            if (serializedNameAnnotation == null) continue;
            String serializedName = serializedNameAnnotation.value();
            if (serializedName.equals("")) continue;
            FIELDS.add(field);
        }
    }
}
