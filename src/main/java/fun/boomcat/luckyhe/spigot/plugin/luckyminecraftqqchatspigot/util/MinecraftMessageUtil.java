package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.SendBindMessageToPlayerFailException;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.Collection;
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

    public static Collection<? extends Player> getOnlinePlayerList() {
        return server.getOnlinePlayers();
    }

    public static void sendMessageToPlayer(String player, String message) throws SendBindMessageToPlayerFailException {
        Player p = server.getPlayer(player);
        if (p == null || !p.getName().equals(player)) {
            throw new SendBindMessageToPlayerFailException();
        }
        p.sendMessage(message);
    }
}
