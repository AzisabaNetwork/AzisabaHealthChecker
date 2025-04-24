package net.azisaba.healthchecker;

import net.azisaba.healthchecker.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {
        try {
            Util.allowMethods("PATCH");
            new AzisabaHealthChecker().start();
        } catch (Throwable throwable) {
            LOGGER.fatal("Failed to start health checker", throwable);
        }
    }
}
