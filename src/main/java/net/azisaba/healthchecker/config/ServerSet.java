package net.azisaba.healthchecker.config;

import net.azisaba.healthchecker.util.InvalidArgumentException;
import net.azisaba.healthchecker.yaml.YamlArray;
import net.azisaba.healthchecker.yaml.YamlObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ServerSet implements Iterable<ConfiguredServer> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<ConfiguredServer> servers = new ArrayList<>();

    public void read(@NotNull YamlArray arr) {
        List<YamlObject> list = arr.mapAsType((Function<Map<String, Object>, YamlObject>) YamlObject::new);
        for (YamlObject obj : list) {
            read(obj);
        }
    }

    public void read(@NotNull YamlObject obj) {
        try {
            servers.add(ConfiguredServer.read(obj));
        } catch (InvalidArgumentException e) {
            LOGGER.warn("Error reading server", e);
        }
    }

    public void clear() {
        servers.clear();
    }

    @NotNull
    @Override
    public Iterator<ConfiguredServer> iterator() {
        return servers.iterator();
    }
}
