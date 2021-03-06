package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.command;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.DataOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.OpIdExistException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.OpIdNotExistException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftFontStyleCode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class McChat implements TabExecutor {
    private JavaPlugin plugin;

    public McChat(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public String addPrefix(String info) {
        return MinecraftFontStyleCode.LIGHT_PURPLE + "[LuckyChat] " + MinecraftFontStyleCode.GOLD + info;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        String commandName = command.getName();

        if (!commandSender.isOp()) {
            commandSender.sendMessage(addPrefix(MinecraftFontStyleCode.RED + "没有使用此命令的权限"));
            return true;
        }

        int len = strings.length;
        if (len == 0) {
            commandSender.sendMessage(addPrefix("帮助菜单："));
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " addop <管理员QQ>    添加一个管理员QQ（可以在群内发送MC端指令）");
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " delop <管理员QQ>    删除一个管理员QQ");
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " listop    列出所有添加op的QQ");
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " reload    重载配置文件");
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
                        Arrays.copyOfRange(strings, 1, len),
                        commandName
                );
                break;
            case "delop":
                onDelOp(
                        commandSender,
                        Arrays.copyOfRange(strings, 1, len),
                        commandName
                );
                break;
            case "listop":
                onListOp(
                        commandSender,
                        commandName
                );
                break;
            default:
                commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " 查看帮助");
                break;
        }
        return true;
    }

    private void onAddOp(CommandSender commandSender, String[] args, String commandName) {
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " addop <管理员QQ>    添加一个管理员QQ（可以在群内发送MC端指令）");
            return;
        }

        String opIdString = args[0];
        long opId;
        try {
            opId = Long.parseLong(opIdString);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " addop <管理员QQ>    添加一个管理员QQ（可以在群内发送MC端指令）");
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

    private void onDelOp(CommandSender commandSender, String[] args, String commandName) {
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " delop <管理员QQ>    删除一个管理员QQ");
            return;
        }

        String opIdString = args[0];
        long opId;
        try {
            opId = Long.parseLong(opIdString);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " delop <管理员QQ>    删除一个管理员QQ");
            return;
        }

        try {
            DataOperation.removeRconCommandIds(opId);
        } catch (OpIdNotExistException e) {
            commandSender.sendMessage(MinecraftFontStyleCode.RED + "管理员QQ " + opId + " 不存在");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage(MinecraftFontStyleCode.RED + "执行指令时出现异常");
            return;
        }

        commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "已删除管理员QQ " + opId);
    }

    private static void onListOp(CommandSender commandSender, String commandName) {
        List<Object> rconCommandOpIds;
        try {
            rconCommandOpIds = DataOperation.getRconCommandOpIds();
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage(MinecraftFontStyleCode.RED + "指令指令时出现异常");
            return;
        }

        StringBuilder res = new StringBuilder();
        for (int i = 0; i < rconCommandOpIds.size(); i++) {
            res.append(rconCommandOpIds.get(i));
            if (i != rconCommandOpIds.size() - 1) {
                res.append(", ");
            }
        }

        commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "已添加op的QQ：" + res);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0 || strings.length == 1) {
            return Arrays.asList("reload", "addop", "delop", "listop");
        } else {
            return Collections.emptyList();
        }
    }
}

