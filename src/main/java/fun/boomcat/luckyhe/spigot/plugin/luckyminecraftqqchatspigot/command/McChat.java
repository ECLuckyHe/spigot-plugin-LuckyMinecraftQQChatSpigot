package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.command;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftFontStyleCode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class McChat implements CommandExecutor {
    private JavaPlugin plugin;

    public McChat(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public String addPrefix(String info) {
        return MinecraftFontStyleCode.LIGHT_PURPLE + "[LuckyChat] " + MinecraftFontStyleCode.GOLD + info;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!commandSender.isOp()) {
            commandSender.sendMessage(addPrefix(MinecraftFontStyleCode.RED + "没有使用此命令的权限"));
            return true;
        }

        int len = strings.length;
        if (len == 0) {
            commandSender.sendMessage(addPrefix("帮助菜单："));
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/mcchat reload    重载配置文件");
            return true;
        }

        String operation = strings[0];
        switch (operation.toLowerCase()) {
            case "reload":
                commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "开始重载LuckyChat插件");
                plugin.onDisable();
                plugin.onEnable();
                break;
        }
        return true;
    }
}
