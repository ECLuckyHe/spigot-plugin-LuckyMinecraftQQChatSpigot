package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.command.McChat;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.command.Qq;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.ConfigOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.DataOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.QqOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.listener.PlayerListener;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.thread.ClientMainThread;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.FormatPlaceholder;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftMessageUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.ReplacePlaceholderUtil;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

//public class LuckyMinecraftQQChatSpigot extends JavaPlugin {
public class LuckyMinecraftQQChatSpigot implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("LuckyMinecraftQQChatFabric");

    private ClientMainThread clientMainThread;
//    @Override
    public void onLoad() {
        try {
            DataOperation.initDataPath(getDataFolder(), getResource("data.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            QqOperation.initDataPath(getDataFolder(), getResource("qq.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        ConfigOperation.initFileConfiguration(getConfig());
        MinecraftMessageUtil.init(getServer(), getLogger());

        this.getCommand("mcchat").setExecutor(new McChat(this));
        this.getCommand("qq").setExecutor(new Qq(this));

        if (clientMainThread == null) {
            clientMainThread = new ClientMainThread(getServer(), getLogger());
            //        启动线程
            clientMainThread.start();
        }

        getLogger().info("开始注册玩家监听器");
        getServer().getPluginManager().registerEvents(new PlayerListener(getLogger(), clientMainThread), this);
        getLogger().info("注册完成");

//        延时一秒
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        不知道原因，但这句语句在reload后不运行，但mcchat reload可以运行
        MinecraftMessageUtil.sendMinecraftMessage(ReplacePlaceholderUtil.replacePlaceholderWithString(
                ConfigOperation.getInfoOnEnable(),
                FormatPlaceholder.SERVER_NAME,
                ConfigOperation.getServerName()
        ));
    }

    @Override
    public void onDisable() {
        getLogger().info("开始关闭连接线程");
        clientMainThread.close();

//        关闭成功则设置为null
        while (clientMainThread.isAlive()) { }
        clientMainThread = null;
        getLogger().info("关闭连接线程成功");

        MinecraftMessageUtil.sendMinecraftMessage(ReplacePlaceholderUtil.replacePlaceholderWithString(
                ConfigOperation.getInfoOnDisable(),
                FormatPlaceholder.SERVER_NAME,
                ConfigOperation.getServerName()
        ));
    }

    @Override
    public void onInitialize() {
        onLoad();
    }

    private File getDataFolder() {

    }

    private InputStream getResource(String path) {

    }

    private void saveDefaultConfig() {

    }

    private void reloadConfig() {

    }

    private Logger getLogger() {
        return LOGGER;
    }
}
