package net.azisaba.healthchecker.config;

import net.azisaba.healthchecker.server.Server;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public enum SelectionStrategy {
    FIRST(servers -> servers.get(0)),
    RANDOM(servers -> servers.get((int) (Math.random() * servers.size()))),
    ROUND_ROBIN(servers -> {
        int index = SelectionStrategyField.ROUND_ROBIN_INDEX.getAndIncrement();
        if (index >= servers.size()) {
            SelectionStrategyField.ROUND_ROBIN_INDEX.set(0);
            index = 0;
        }
        return servers.get(index);
    }),
    ;

    private final Function<List<Server>, Server> selector;

    SelectionStrategy(@NotNull Function<List<Server>, Server> selector) {
        this.selector = selector;
    }

    public @NotNull Server select(@NotNull List<Server> servers) {
        if (servers.isEmpty()) {
            throw new IllegalArgumentException("servers must not be empty");
        }
        return selector.apply(servers);
    }
}
