package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.listener;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.ConfigOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.thread.ClientMainThread;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.ConnectionPacketSendUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.bean.UserBean;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import static org.bukkit.event.EventPriority.MONITOR;

public class PlayerListener implements Listener {
    private final Logger logger;
    private final ClientMainThread clientMainThread;
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    public PlayerListener(Logger logger, ClientMainThread clientMainThread) {
        this.logger = logger;
        this.clientMainThread = clientMainThread;
    }

    @EventHandler(priority = MONITOR)
    public void onPlayerMessageEvent(AsyncPlayerChatEvent e) {
        if (e.isCancelled() ){//忽略已被标记为取消的聊天事件
                return;
            }
        String formatMessage = ConfigOperation.getFormatMessage();
        if (formatMessage != null && (!formatMessage.equals("")) && clientMainThread.isAlive()) {
            clientMainThread.addSendQueue(ConnectionPacketSendUtil.getMessagePacket(
                    e.getPlayer().getName(),
                    e.getMessage()
            ));
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        String formatJoin = ConfigOperation.getFormatJoin();
        if (formatJoin != null && (!formatJoin.equals("")) && clientMainThread.isAlive()) {
            clientMainThread.addSendQueue(ConnectionPacketSendUtil.getJoinPacket(e.getPlayer().getName()));
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        String formatQuit = ConfigOperation.getFormatQuit();
        if (formatQuit != null && (!formatQuit.equals("")) && clientMainThread.isAlive()) {
            clientMainThread.addSendQueue(ConnectionPacketSendUtil.getQuitPacket(e.getPlayer().getName()));
        }
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent e) {
        String formatDeath = ConfigOperation.getFormatDeath();
        if (formatDeath != null && (!formatDeath.equals("")) && clientMainThread.isAlive()) {
            clientMainThread.addSendQueue(ConnectionPacketSendUtil.getDeathMessagePacket(e.getEntity().getName(), e.getDeathMessage()));
        }
    }

    @EventHandler
    public void onPlayerKickEvent(PlayerKickEvent e) {
        String formatKick = ConfigOperation.getFormatKick();
        if (formatKick != null && (!formatKick.equals("")) && clientMainThread.isAlive()) {
            clientMainThread.addSendQueue(ConnectionPacketSendUtil.getKickMessagePacket(e.getPlayer().getName(), e.getReason()));
        }
    }

    @EventHandler
    public void onPlayerLoginEvent(PlayerLoginEvent e) {
        if (!Bukkit.hasWhitelist() || !ConfigOperation.getWhitelistEnabled()) {
//            白名单和白名单修复都必须开启，否则进入这里
            return;
        }

        String whitelistCorrectMessage = ConfigOperation.getWhitelistCorrectMessage();
        String whitelistTryMessage = ConfigOperation.getWhitelistTryMessage();

        PlayerLoginEvent.Result result = e.getResult();
        if (!(result.equals(PlayerLoginEvent.Result.KICK_WHITELIST))) {
//            不是因为白名单而踢出的，不予理会
            return;
        }

        Player player = e.getPlayer();
        String name = player.getName();
        UUID uniqueId = player.getUniqueId();

        if (whitelistTryMessage != null && (!whitelistTryMessage.equals("")) && clientMainThread.isAlive()) {
//            发送尝试进入消息
            clientMainThread.addSendQueue(ConnectionPacketSendUtil.getWhitelistTryMessage(name));
        }

//        在白名单中搜寻该玩家
        boolean isWhitelist = false, isUuidCorrect = true;
        Set<OfflinePlayer> whitelistedPlayers = Bukkit.getWhitelistedPlayers();
        for (OfflinePlayer whitelistedPlayer : whitelistedPlayers) {
            if (Objects.requireNonNull(whitelistedPlayer.getName()).toLowerCase().equals(name)) {
                isWhitelist = true;
                if (!whitelistedPlayer.getUniqueId().toString().toLowerCase().equals(uniqueId.toString())) {
                    isUuidCorrect = false;
                }
                break;
            }
        }

        if (!isWhitelist) {
//            玩家不属于白名单，就不管了
            return;
        }

        if (isUuidCorrect) {
//            玩家属于白名单，且uuid一致，此时进入游戏成功，不管了
            return;
        }

//        接下来的为修改uuid逻辑

        List<UserBean> userBeans;
        File file = new File("whitelist.json");
//        读取并修改
        lock.readLock().lock();
        try {
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            userBeans = JSONObject.parseArray(content, UserBean.class);
            for (UserBean userBean : userBeans) {
                if (userBean.getName().toLowerCase().equals(name)) {
                    userBean.setUuid(uniqueId.toString());
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return;
        } finally {
            lock.readLock().unlock();
        }

//        保存修改
        lock.writeLock().lock();
        try {
            String content = JSONObject.toJSONString(userBeans, SerializerFeature.PrettyFormat);
            FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            exception.printStackTrace();
            return;
        } finally {
            lock.writeLock().unlock();
        }

        Bukkit.reloadWhitelist();

        if (whitelistCorrectMessage != null && (!whitelistCorrectMessage.equals("")) && clientMainThread.isAlive()) {
//            发送尝试进入消息
            clientMainThread.addSendQueue(ConnectionPacketSendUtil.getWhitelistCorrectMessage(name));
        }
    }
}
