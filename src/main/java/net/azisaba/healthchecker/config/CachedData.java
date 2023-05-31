package net.azisaba.healthchecker.config;

import net.azisaba.healthchecker.yaml.YamlObject;
import org.jetbrains.annotations.NotNull;

public class CachedData {
    public boolean wasDown;
    public long downSince;

    public CachedData(boolean wasDown, long downSince) {
        this.wasDown = wasDown;
        this.downSince = downSince;
    }

    @NotNull
    public static CachedData read(@NotNull YamlObject obj) {
        boolean wasDown = obj.getBoolean("wasDown", false);
        long downSince = obj.getLong("downSince", 0);
        return new CachedData(wasDown, downSince);
    }

    @NotNull
    public YamlObject save() {
        YamlObject obj = new YamlObject();
        obj.set("wasDown", wasDown);
        obj.set("downSince", downSince);
        return obj;
    }
}
