package net.azisaba.healthchecker.config;

import java.util.concurrent.atomic.AtomicInteger;

class SelectionStrategyField {
    static final AtomicInteger ROUND_ROBIN_INDEX = new AtomicInteger(0);
}
