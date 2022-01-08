package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.logging.Logger;

public class PlayerListener implements Listener {
    private Logger logger;

    public PlayerListener(Logger logger) {
        this.logger = logger;
    }

    @EventHandler
    public void onPlayerMessage(AsyncPlayerChatEvent e) {

    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {

    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {

    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent e) {

    }

    @EventHandler
    public void onPlayerKickEvent(PlayerKickEvent e) {

    }
}
