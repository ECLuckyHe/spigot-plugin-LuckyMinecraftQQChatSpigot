package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.listener;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.ConfigOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.thread.ClientMainThread;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.ConnectionPacketSendUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.logging.Logger;

public class PlayerListener implements Listener {
    private final Logger logger;
    private final ClientMainThread clientMainThread;

    public PlayerListener(Logger logger, ClientMainThread clientMainThread) {
        this.logger = logger;
        this.clientMainThread = clientMainThread;
    }

    @EventHandler
    public void onPlayerMessageEvent(AsyncPlayerChatEvent e) {
        String formatMessage = ConfigOperation.getFormatMessage();
        if (formatMessage != null && (!formatMessage.equals(""))) {
            clientMainThread.addSendQueue(ConnectionPacketSendUtil.getMessagePacket(
                    e.getPlayer().getName(),
                    e.getMessage()
            ));
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        System.out.println("e = " + e);
        System.out.println("e.getPlayer().getName() = " + e.getPlayer().getName());

        String formatJoin = ConfigOperation.getFormatJoin();
        if (formatJoin != null && (!formatJoin.equals(""))) {
            clientMainThread.addSendQueue(ConnectionPacketSendUtil.getJoinPacket(e.getPlayer().getName()));
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        String formatQuit = ConfigOperation.getFormatQuit();
        if (formatQuit != null && (!formatQuit.equals(""))) {
            clientMainThread.addSendQueue(ConnectionPacketSendUtil.getQuitPacket(e.getPlayer().getName()));
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent e) {
        String formatDeath = ConfigOperation.getFormatDeath();
        if (formatDeath != null && (!formatDeath.equals(""))) {
            clientMainThread.addSendQueue(ConnectionPacketSendUtil.getDeathMessagePacket(e.getEntity().getName(), e.getDeathMessage()));
        }
    }

    @EventHandler
    public void onPlayerKickEvent(PlayerKickEvent e) {
        String formatKick = ConfigOperation.getFormatKick();
        if (formatKick != null && (!formatKick.equals(""))) {
            clientMainThread.addSendQueue(ConnectionPacketSendUtil.getKickMessagePacket(e.getPlayer().getName(), e.getReason()));
        }
    }
}
