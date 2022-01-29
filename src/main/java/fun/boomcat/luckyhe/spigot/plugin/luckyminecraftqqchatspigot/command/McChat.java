package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.command;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.DataOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.OpIdExistException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.OpIdNotExistException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftFontStyleCode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

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
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/mcchat addop <管理员QQ>    添加一个管理员QQ（可以在群内发送MC端指令）");
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/mcchat delop <管理员QQ>    删除一个管理员QQ");
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
            case "addop":
                onAddOp(
                        commandSender,
                        Arrays.copyOfRange(strings, 1, len)
                );
                break;
            case "delop":
                onDelOp(
                        commandSender,
                        Arrays.copyOfRange(strings, 1, len)
                );
                break;
            default:
                commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/mcchat 查看帮助");
                break;
        }
        return true;
    }

    private void onAddOp(CommandSender commandSender, String[] args) {
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/mcchat addop <管理员QQ>    添加一个管理员QQ（可以在群内发送MC端指令）");
            return;
        }

        String opIdString = args[0];
        long opId;
        try {
            opId = Long.parseLong(opIdString);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/mcchat addop <管理员QQ>    添加一个管理员QQ（可以在群内发送MC端指令）");
            return;
        }

        try {
            DataOperation.addRconCommandOpIds(opId);
        } catch (OpIdExistException e) {
            commandSender.sendMessage(MinecraftFontStyleCode.RED + "管理员QQ " + opId + " 已存在");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage(MinecraftFontStyleCode.RED + "执行指令时出现异常");
            return;
        }

        commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "已添加管理员QQ " + opId);
    }

    private void onDelOp(CommandSender commandSender, String[] args) {
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/mcchat delop <管理员QQ>    删除一个管理员QQ");
            return;
        }

        String opIdString = args[0];
        long opId;
        try {
            opId = Long.parseLong(opIdString);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/mcchat delop <管理员QQ>    删除一个管理员QQ");
            return;
        }

        try {
            DataOperation.removeRconCommnadIds(opId);
        } catch (OpIdNotExistException e) {
            commandSender.sendMessage(MinecraftFontStyleCode.RED + "管理员QQ " + opId + " 不存在");
            return;
        } catch (Exception e) {
            e.printStackTrace();;
            commandSender.sendMessage(MinecraftFontStyleCode.RED + "执行指令时出现异常");
            return;
        }

        commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "已删除管理员QQ " + opId);
    }
}

