package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.ConfigOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.listener.PlayerListener;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.thread.ClientMainThread;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftMessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class LuckyMinecraftQQChatSpigot extends JavaPlugin {

    private ClientMainThread clientMainThread;
    @Override
    public void onLoad() {
        saveDefaultConfig();
        ConfigOperation.initFileConfiguration(getConfig());
        MinecraftMessageUtil.init(getServer(), getLogger());
    }

    @Override
    public void onEnable() {
        clientMainThread = new ClientMainThread(getLogger());
        //        启动线程
        clientMainThread.start();

        getLogger().info("开始注册玩家监听器");
        getServer().getPluginManager().registerEvents(new PlayerListener(getLogger(), clientMainThread), this);
        getLogger().info("注册完成");


    }

    @Override
    public void onDisable() {
        getLogger().info("开始关闭连接线程");
        clientMainThread.close();

        while (!clientMainThread.isSocketClosed()) {}
        getLogger().info("关闭连接线程成功");
    }
}