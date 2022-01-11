package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.command.McChat;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.ConfigOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.listener.PlayerListener;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.thread.ClientMainThread;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftFontStyleCode;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftMessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class LuckyMinecraftQQChatSpigot extends JavaPlugin {

    private ClientMainThread clientMainThread;
    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        ConfigOperation.initFileConfiguration(getConfig());
        MinecraftMessageUtil.init(getServer(), getLogger());

        this.getCommand("mcchat").setExecutor(new McChat(this));

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

//        不知道原因，但这句语句不运行
        MinecraftMessageUtil.sendMinecraftMessage(
                MinecraftFontStyleCode.LIGHT_PURPLE + "[LuckyChat] " +
                        MinecraftFontStyleCode.GOLD + "插件被加载"
        );

    }

    @Override
    public void onDisable() {
        getLogger().info("开始关闭连接线程");
        clientMainThread.close();

//        关闭成功则设置为null
        while (clientMainThread.isAlive()) { }
        clientMainThread = null;
        getLogger().info("关闭连接线程成功");

        MinecraftMessageUtil.sendMinecraftMessage(
                MinecraftFontStyleCode.LIGHT_PURPLE + "[LuckyChat] " +
                        MinecraftFontStyleCode.GOLD + "插件被卸载"
        );
    }
}