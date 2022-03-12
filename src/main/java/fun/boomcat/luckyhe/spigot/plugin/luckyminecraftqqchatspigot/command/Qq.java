package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.command;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.QqOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.BindQqUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftFontStyleCode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Qq implements CommandExecutor {
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
            commandSender.sendMessage(addPrefix("帮助菜单："));
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " list    查看申请绑定此id的QQ");
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " deny <QQ>    拒绝该QQ要求与该id绑定的申请");
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " confirm <QQ>    同意该QQ要求与id绑定的申请");
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " bound    查看已绑定此id的QQ");
            commandSender.sendMessage(MinecraftFontStyleCode.GOLD + "/" + commandName + " unbind <QQ>    将该QQ与该id解绑");
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

                break;
            }
        }

        return true;
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
}
