package net.azisaba.healthChecker.config;

import net.azisaba.healthChecker.yaml.YamlConfiguration;
import net.azisaba.healthChecker.yaml.YamlObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CacheFile {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Map<String, CachedData> map = new HashMap<>();

    public static void load() {
        LOGGER.info("Loading cache.yml");
        try {
            map.clear();
            File file = new File("./cache.yml");
            if (!file.exists() && !file.createNewFile()) {
                LOGGER.warn("Failed to create " + file.getAbsolutePath());
            }
            YamlObject obj = new YamlConfiguration(file).asObject();
            for (String key : obj.getRawData().keySet()) {
                YamlObject o = obj.getObject(key);
                if (o != null) map.put(key, CachedData.read(o));
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to load cache.yml", e);
        }
    }

    public static void save() {
        try {
            File file = new File("./cache.yml");
            if (!file.exists() && !file.createNewFile()) {
                LOGGER.warn("Failed to create " + file.getAbsolutePath());
            }
            YamlObject obj = new YamlObject();
            map.forEach((key, value) -> obj.setObject(key, value.save()));
            obj.save(file);
        } catch (IOException e) {
            LOGGER.warn("Failed to save cache.yml", e);
        }
    }
}
