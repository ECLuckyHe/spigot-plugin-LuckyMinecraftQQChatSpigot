package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util;

import org.bukkit.Server;

import java.util.logging.Logger;

public class MinecraftMessageUtil {
    private static Server server;
    private static Logger logger;

    public static void init(Server s, Logger l) {
        server = s;
        logger = l;
    }

    public static void sendMinecraftMessage(String message) {
        server.broadcastMessage(message);
    }
}
