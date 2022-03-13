package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.command;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.QqOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.UserBindNotExistException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.pojo.Packet;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.BindQqUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftFontStyleCode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Qq implements TabExecutor {
    private JavaPlugin plugin;

    public Qq(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public String addPrefix(String info) {
        return MinecraftFontStyleCode.LIGHT_PURPLE + "[LuckyChat] " + MinecraftFontStyleCode.GOLD + info;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        String commandName = command.getName();

        int len = strings.length;
        if (len == 0) {
            onHelp(commandSender, commandName);
            return true;
        }

        String operation = strings[0];
        switch (operation.toLowerCase()) {
            case "list": {
//                列出绑定
                List<Long> qqsByMcid = BindQqUtil.getQqsByMcid(commandSender.getName());

                String head;
                if (qqsByMcid.size() == 0) {
                    head = MinecraftFontStyleCode.GOLD + "无qq申请与该id绑定";
                } else {
                    head = MinecraftFontStyleCode.GOLD + "以下QQ正在申请与此id绑定：";
                }
                StringBuilder sb = new StringBuilder(head);

                for (long qq : qqsByMcid) {
                    sb.append(MinecraftFontStyleCode.GREEN).append(qq).append(MinecraftFontStyleCode.GOLD).append(", ");
                }

                commandSender.sendMessage(qqsByMcid.size() == 0 ? sb.toString() : sb.substring(0, sb.length() - 2));

                break;
            }
            case "deny": {
                onDeny(Arrays.copyOfRange(strings, 1, strings.length), commandSender, commandName);
                break;
            }
            case "confirm": {
                onConfirm(Arrays.copyOfRange(strings, 1, strings.length), commandSender, commandName);
                break;
            }
            case "bound": {
                List<Long> qqsByMcid;
                try {
                    qqsByMcid = QqOperation.getQqsByMcid(commandSender.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                    commandSender.sendMessage(MinecraftFontStyleCode.RED + "在获取绑定列表时出现异常，请稍候重试或联系开发者");
                    break;
                }

                if (qqsByMcid.size() == 0) {
                    commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "无QQ与该id绑定");
                } else {
                    StringBuilder sb = new StringBuilder(MinecraftFontStyleCode.GOLD + "这些QQ与该id绑定：");
                    for (long qq : qqsByMcid) {
                        sb.append(MinecraftFontStyleCode.GREEN).append(qq).append(MinecraftFontStyleCode.GOLD).append(", ");
                    }
                    commandSender.sendMessage(sb.substring(0, sb.length() - 2));
                }
                break;
            }
            case "unbind": {
                onUnbind(Arrays.copyOfRange(strings, 1, strings.length), commandSender, commandName);
                break;
            }
            default: {
                onHelp(commandSender, commandName);
                break;
            }
        }

        return true;
    }

    private void onHelp(CommandSender commandSender, String commandName) {
        commandSender.sendMessage(addPrefix("帮助菜单："));
        commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " list    查看申请绑定此id的QQ");
        commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " deny <QQ>    拒绝该QQ要求与该id绑定的申请");
        commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " confirm <QQ>    同意该QQ要求与id绑定的申请");
        commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " bound    查看已绑定此id的QQ");
        commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " unbind <QQ>    将该QQ与该id解绑");
    }

    private void onDeny(Object[] args, CommandSender commandSender, String commandName) {
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " deny <QQ>    拒绝该QQ要求与该id绑定的申请");
            return;
        }

        String qqString = args[0].toString();
        long qq;
        try {
            qq = Long.parseLong(qqString);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " deny <QQ>    拒绝该QQ要求与该id绑定的申请");
            return;
        }

//          获取申请绑定该id的qq列表，判断
        List<Long> qqsByMcid = BindQqUtil.getQqsByMcid(commandSender.getName());
        if (!qqsByMcid.contains(qq)) {
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "申请列表中无该QQ");
        } else {
            BindQqUtil.denyBind(qq, commandSender.getName());
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "已拒绝QQ " + MinecraftFontStyleCode.GREEN + qq + MinecraftFontStyleCode.GOLD + "的绑定申请");
        }
    }

    private void onConfirm(Object[] args, CommandSender commandSender, String commandName) {
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " confirm <QQ>    同意该QQ要求与id绑定的申请");
            return;
        }

        String qqString = args[0].toString();
        long qq;
        try {
            qq = Long.parseLong(qqString);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " confirm <QQ>    同意该QQ要求与id绑定的申请");
            return;
        }

        List<Long> qqsByMcid = BindQqUtil.getQqsByMcid(commandSender.getName());
        if (!qqsByMcid.contains(qq)) {
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "申请列表中无该QQ");
        } else {
            try {
                BindQqUtil.confirmBind(qq, commandSender.getName());
            } catch (IOException e) {
                e.printStackTrace();
                commandSender.sendMessage(MinecraftFontStyleCode.RED + "在同意申请时发生异常，请稍候重试或联系开发者");
                return;
            }

            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "已同意QQ " + MinecraftFontStyleCode.GREEN + qq + MinecraftFontStyleCode.GOLD + "的绑定申请");
        }
    }

    private void onUnbind(Object[] args, CommandSender commandSender, String commandName) {
        int len = args.length;
        if (len == 0) {
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " unbind <QQ>    将该QQ与该id解绑");
            return;
        }

        String unbindQqString = args[0].toString();
        long unbindQq;
        try {
            unbindQq = Long.parseLong(unbindQqString);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " unbind <QQ>    将该QQ与该id解绑");
            return;
        }

        try {
            QqOperation.unbind(unbindQq, commandSender.getName());
        } catch (UserBindNotExistException e) {
            commandSender.sendMessage(MinecraftFontStyleCode.RED + "解绑失败：该QQ未与该id绑定");
            return;
        } catch (Exception e) {
            e.printStackTrace();
            commandSender.sendMessage(MinecraftFontStyleCode.RED + "在解绑时发生异常，请稍候重试或联系开发者");
            return;
        }

        commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "已成功解绑QQ " + MinecraftFontStyleCode.GREEN + unbindQq);
    }


    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> list = new ArrayList<>();
//        考虑未传入参数的情况
        boolean isReadyInputNext = true;
        for (int i = 0; i < strings.length; i++) {
            if (!strings[i].equals("")) {
                list.add(strings[i]);
                isReadyInputNext = false;
            } else if (i == strings.length - 1) {
                isReadyInputNext = true;
            }
        }

        int current = list.size() + (isReadyInputNext ? 1 : 0);
        switch (current) {
            case 1: {
                return Arrays.asList("list", "deny", "confirm", "bound", "unbind");
            }
            case 2: {
                switch (list.get(0)) {
                    case "unbind": {
                        List<Long> qqsByMcid;
                        try {
                            qqsByMcid = QqOperation.getQqsByMcid(commandSender.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Collections.emptyList();
                        }

                        List<String> res = new ArrayList<>();
                        for (long qq : qqsByMcid) {
                            res.add(String.valueOf(qq));
                        }

                        return res;
                    }
                    case "confirm":
                    case "deny": {
                        List<Long> qqsByMcid = BindQqUtil.getQqsByMcid(commandSender.getName());
                        List<String> res = new ArrayList<>();
                        for (long qq : qqsByMcid) {
                            res.add(String.valueOf(qq));
                        }

                        return res;
                    }
                    case "list":
                    case "bound":
                    default: {
                        return Collections.emptyList();
                    }
                }
            }
            default: {
                return Collections.emptyList();
            }
        }
    }
}
