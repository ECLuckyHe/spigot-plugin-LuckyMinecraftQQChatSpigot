package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.ConfigOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public class LuckyMinecraftQQChatSpigot extends JavaPlugin {
    @Override
    public void onLoad() {
        saveDefaultConfig();
        ConfigOperation.initFileConfiguration(getConfig());
    }

    @Override
    public void onEnable() {
        getLogger().info("开始注册玩家监听器");
        getServer().getPluginManager().registerEvents(new PlayerListener(getLogger()), this);
        getLogger().info("注册完成");
    }

    @Override
    public void onDisable() {
        
    }
}