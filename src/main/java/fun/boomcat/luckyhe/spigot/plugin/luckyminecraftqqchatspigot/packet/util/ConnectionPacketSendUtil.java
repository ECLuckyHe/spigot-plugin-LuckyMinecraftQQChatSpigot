package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.DataOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.UserCommandExistException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.UserCommandNotExistException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarInt;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarLong;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarIntStringLengthNotMatchException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarIntTooBigException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.pojo.Packet;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftMessageUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.UserCommandUtil;
import org.bukkit.entity.Player;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ConnectionPacketSendUtil {
    public static Packet getConnectPacket(
            long sessionId,
            String serverName,
            String joinFormatString,
            String quitFormatString,
            String msgFormatString,
            String deathFormatString,
            String kickFormatString,
            List<String> onlinePlayersCommands,
            String onlinePlayersCommandResponseFormat,
            String onlinePlayersCommandResponseSeparator,
            String rconCommandPrefix,
            String rconCommandResultFormat,
            String userCommandPrefix,
            String userBindPrefix
    ) {
        VarInt packetId = new VarInt(0x00);

        VarLong si = new VarLong(sessionId);
        VarIntString sn = new VarIntString(serverName);
        VarIntString jfs = new VarIntString(joinFormatString);
        VarIntString qfs = new VarIntString(quitFormatString);
        VarIntString mfs = new VarIntString(msgFormatString);
        VarIntString dfs = new VarIntString(deathFormatString);
        VarIntString kfs = new VarIntString(kickFormatString);

        VarInt opcc = new VarInt(onlinePlayersCommands.size());
        VarIntString[] opccs = new VarIntString[opcc.getValue()];
        for (int i = 0; i < opccs.length; i++) {
            opccs[i] = new VarIntString(onlinePlayersCommands.get(i));
        }
        VarIntString opcrf = new VarIntString(onlinePlayersCommandResponseFormat);
        VarIntString opcrs = new VarIntString(onlinePlayersCommandResponseSeparator);

        VarIntString rcp = new VarIntString(rconCommandPrefix);
        VarIntString rcrf = new VarIntString(rconCommandResultFormat);

        VarIntString ucp = new VarIntString(userCommandPrefix);
        VarIntString ubp = new VarIntString(userBindPrefix);

        int totalLengthInt = packetId.getBytesLength() + si.getBytesLength() + sn.getBytesLength() +
                jfs.getBytesLength() + qfs.getBytesLength() + mfs.getBytesLength() + dfs.getBytesLength() +
                kfs.getBytesLength() + opcc.getBytesLength() + opcrf.getBytesLength() + opcrs.getBytesLength() +
                rcp.getBytesLength() + rcrf.getBytesLength() + ucp.getBytesLength() + ubp.getBytesLength();
        for (VarIntString opcca : opccs) {
            totalLengthInt += opcca.getBytesLength();
        }

        byte[] data = ByteUtil.byteMergeAll(
                si.getBytes(),
                sn.getBytes(),
                jfs.getBytes(),
                qfs.getBytes(),
                mfs.getBytes(),
                dfs.getBytes(),
                kfs.getBytes(),
                opcc.getBytes()
        );
        for (VarIntString opcca : opccs) {
            data = ByteUtil.byteMergeAll(data, opcca.getBytes());
        }
        data = ByteUtil.byteMergeAll(
                data,
                opcrf.getBytes(),
                opcrs.getBytes(),
                rcp.getBytes(),
                rcrf.getBytes(),
                ucp.getBytes(),
                ubp.getBytes()
        );

        return new Packet(
                new VarInt(totalLengthInt),
                packetId,
                data
        );
    }

    public static Packet getJoinPacket(String joinPlayer) {
        VarInt packetId = new VarInt(0x10);
        VarIntString joinPlayerString = new VarIntString(joinPlayer);
        return new Packet(new VarInt(packetId.getBytesLength() + joinPlayerString.getBytesLength()), packetId, joinPlayerString.getBytes());
    }

    public static Packet getQuitPacket(String quitPlayer) {
        VarInt packetId = new VarInt(0x11);
        VarIntString quitPlayerString = new VarIntString(quitPlayer);
        return new Packet(new VarInt(packetId.getBytesLength() + quitPlayerString.getBytesLength()), packetId, quitPlayerString.getBytes());
    }

    public static Packet getMessagePacket(String player, String message) {
        VarInt packetId = new VarInt(0x12);
        VarIntString playerString = new VarIntString(player);
        VarIntString messageString = new VarIntString(message);
        return new Packet(
                new VarInt(packetId.getBytesLength() + playerString.getBytesLength() + messageString.getBytesLength()),
                packetId,
                ByteUtil.byteMergeAll(playerString.getBytes(), messageString.getBytes())
        );
    }

    public static Packet getDeathMessagePacket(String player, String deathMessage) {
        VarInt packetId = new VarInt(0x13);
        VarIntString playerString = new VarIntString(player);
        VarIntString deathMessageString = new VarIntString(deathMessage);
        return new Packet(
                new VarInt(packetId.getBytesLength() + playerString.getBytesLength() + deathMessageString.getBytesLength()),
                packetId,
                ByteUtil.byteMergeAll(playerString.getBytes(), deathMessageString.getBytes())
        );
    }

    public static Packet getKickMessagePacket(String player, String kickReason) {
        VarInt packetId = new VarInt(0x14);
        VarIntString playerString = new VarIntString(player);
        VarIntString kickReasonString = new VarIntString(kickReason);
        return new Packet(
                new VarInt(packetId.getBytesLength() + playerString.getBytesLength() + kickReasonString.getBytesLength()),
                packetId,
                ByteUtil.byteMergeAll(playerString.getBytes(), kickReasonString.getBytes())
        );
    }

    public static Packet getClosePacket(String info) {
        VarInt packetId = new VarInt(0xF0);
        VarIntString infoString = new VarIntString(info);
        return new Packet(
                new VarInt(packetId.getBytesLength() + infoString.getBytesLength()),
                packetId,
                infoString.getBytes()
        );
    }

    public static Packet getPongPacket(long ping) {
        VarInt packetId = new VarInt(0x20);
        VarLong pong = new VarLong(ping);
        return new Packet(
                new VarInt(packetId.getBytesLength() + pong.getBytesLength()),
                packetId,
                pong.getBytes()
        );
    }

    public static Packet getOnlinePlayersPacket() throws VarIntStringLengthNotMatchException, IOException, VarIntTooBigException {
        VarInt packetId = new VarInt(0x21);
        Collection<? extends Player> onlinePlayerList =
                MinecraftMessageUtil.getOnlinePlayerList();
        VarInt onlinePlayersCount = new VarInt(onlinePlayerList.size());
        byte[][] onlinePlayerData = new byte[onlinePlayersCount.getValue()][];

        int index = 0;
        for (Player player : onlinePlayerList) {
            onlinePlayerData[index] = new VarIntString(player.getName()).getBytes();
            index += 1;
        }

        byte[] mergeAll = ByteUtil.byteMergeAll(onlinePlayerData);

        return new Packet(
                new VarInt(packetId.getBytesLength() + onlinePlayersCount.getBytesLength() + mergeAll.length),
                packetId,
                ByteUtil.byteMergeAll(onlinePlayersCount.getBytes(), mergeAll)
        );
    }

    public static Packet getRconCommandRefusedPacket(boolean rconEnable) {
        VarInt packetId = new VarInt(0x22);
        VarIntString commandResult;
        if (!rconEnable) {
            commandResult = new VarIntString("MC服务端未开启RCON指令操作，指令执行失败");
        } else {
            commandResult = new VarIntString("无操作权限");
        }
        return new Packet(
                new VarInt(packetId.getBytesLength() + commandResult.getBytesLength()),
                packetId,
                commandResult.getBytes()
        );
    }

    public static Packet getRconCommandResultPacket(boolean rconEnable, String command) {
        VarInt packetId = new VarInt(0x22);
        VarIntString commandResult;
        if (!rconEnable) {
            commandResult = new VarIntString("MC服务端未开启RCON指令操作，指令执行失败");
        } else {
            if (command.contains("reload")) {
                commandResult = new VarIntString("拒绝执行具有reload的指令");
            } else {
//                执行指令
                try {
                    commandResult = new VarIntString(RconUtil.sendMcCommad(command));
                } catch (Exception e) {
                    commandResult = new VarIntString("指令执行失败");
                }
            }
        }

        return new Packet(
                new VarInt(packetId.getBytesLength() + commandResult.getBytesLength()),
                packetId,
                commandResult.getBytes()
        );
    }

    public static Packet getAddUserCommandResultPacket(long id, String name, String userCommand, String mapCommand) {
//        添加用户指令返回结果
        VarInt packetId = new VarInt(0x25);

//        op验证
        boolean rconCommandOpIdExist = false;
        try {
            rconCommandOpIdExist = DataOperation.isRconCommandOpIdExist(id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (!rconCommandOpIdExist) {
            VarIntString res = new VarIntString("用户 " + id + " 不是MC端远程op，请在MC端执行以下指令获得op权限：\n" +
                    "/mcchat addop " + id);
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

//        获取一遍
        Map<String, String> commandMap;
        try {
            commandMap = DataOperation.getRconCommandUserCommandByName(name);
        } catch (Exception e) {
            VarIntString res = new VarIntString("出现异常，请稍后重试");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

//        若找到了，则说明已存在，不进行添加并发送已存在的指令信息
        if (commandMap != null) {
            String n = commandMap.get("name");
            String c = commandMap.get("command");
            String m = commandMap.get("mapping");
            VarIntString res = new VarIntString("用户指令 " + name + " 已存在：\n" +
                    "指令名：" + n + "\n" +
                    "用户指令：" + c + "\n" +
                    "实际指令：" + m);
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

//        审核是否按照格式要求
        userCommand = UserCommandUtil.craftCommand(UserCommandUtil.splitCommand(userCommand));
        mapCommand = UserCommandUtil.craftCommand(UserCommandUtil.splitCommand(mapCommand));
        List<String> userCommandStrings = UserCommandUtil.getCommandArgList(userCommand);
        List<String> mapCommandStrings = UserCommandUtil.getCommandArgList(mapCommand);

        for (String mapCommandString : mapCommandStrings) {
            userCommandStrings.removeIf(o -> o.equals(mapCommandString));
        }

//        有未使用的参数
        if (userCommandStrings.size() != 0) {
            StringBuilder sb = new StringBuilder("创建用户指令失败，以下参数未使用：\n");
            for (String s : userCommandStrings) {
                sb.append(s).append("\n");
            }
            VarIntString res = new VarIntString(sb.toString());
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

//        正式添加
        try {
            DataOperation.addRconCommandUserCommand(name, userCommand, mapCommand);
        } catch (UserCommandExistException e) {
            e.printStackTrace();
        } catch (Exception e) {
            VarIntString res = new VarIntString("出现异常，请稍后重试");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

//        再获取一次
        try {
            commandMap = DataOperation.getRconCommandUserCommandByName(name);
        } catch (Exception e) {
            VarIntString res = new VarIntString("出现异常，请稍后重试");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

        String n = commandMap.get("name");
        String c = commandMap.get("command");
        String m = commandMap.get("mapping");
        VarIntString res = new VarIntString("用户指令 " + name + " 已添加：\n" +
                "指令名：" + n + "\n" +
                "用户指令：" + c + "\n" +
                "实际指令：" + m);
        return new Packet(
                new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                packetId,
                res.getBytes()
        );
    }

    public static Packet getDelUserCommandResultPacket(long id, String name) {
//        删除用户指令返回
        VarInt packetId = new VarInt(0x26);

        //        op验证
        boolean rconCommandOpIdExist = false;
        try {
            rconCommandOpIdExist = DataOperation.isRconCommandOpIdExist(id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (!rconCommandOpIdExist) {
            VarIntString res = new VarIntString("用户 " + id + " 不是MC端远程op，请在MC端执行以下指令获得op权限：\n" +
                    "/mcchat addop " + id);
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

//        获取一次
        Map<String, String> commandMap = null;
        try {
            commandMap = DataOperation.getRconCommandUserCommandByName(name);
        } catch (Exception e) {
            VarIntString res = new VarIntString("出现异常，请稍后重试");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

//        判断，若不存在则不删除
        if (commandMap == null) {
            VarIntString res = new VarIntString("不存在用户指令" + name);
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

//        删除操作
        try {
            DataOperation.delRconCommandUserCommand(name);
        } catch (UserCommandNotExistException e) {
            e.printStackTrace();
        } catch (Exception e) {
            VarIntString res = new VarIntString("出现异常，请稍后重试");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

        boolean isExist = true;
        try {
            isExist = DataOperation.isRconCommandUserCommandNameExist(name);
        } catch (Exception e) {
            VarIntString res = new VarIntString("出现异常，请稍后重试");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

        if (isExist) {
            VarIntString res = new VarIntString("出现异常，请稍后重试");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

        String n = commandMap.get("name");
        String c = commandMap.get("command");
        String m = commandMap.get("mapping");
        VarIntString res = new VarIntString("用户指令" + name + "已删除：\n" +
                "指令名：" + n + "\n" +
                "用户指令：" + c + "\n" +
                "实际指令：" + m);
        return new Packet(
                new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                packetId,
                res.getBytes()
        );
    }

    public static Packet getMcChatUserCommandResultPacket() throws FileNotFoundException {
//        mcchat指令获取用户指令列表
        VarInt packetId = new VarInt(0x27);
        List<Map<String, String>> rconCommandUserCommands = DataOperation.getRconCommandUserCommands();
        VarInt commandLength = new VarInt(rconCommandUserCommands.size());

        int packetLength = packetId.getBytesLength() + commandLength.getBytesLength();
        byte[] data = commandLength.getBytes();
        for (Map<String, String> map : rconCommandUserCommands) {
            VarIntString name = new VarIntString(map.get("name"));
            VarIntString command = new VarIntString(map.get("command"));
            VarIntString mapping = new VarIntString(map.get("mapping"));
            packetLength += name.getBytesLength() + command.getBytesLength() + mapping.getBytesLength();
            data = ByteUtil.byteMergeAll(
                    data,
                    name.getBytes(),
                    command.getBytes(),
                    mapping.getBytes()
            );
        }

        return new Packet(
                new VarInt(packetLength),
                packetId,
                data
        );
    }

    public static Packet getUserBindResultPacket(long senderId, String mcid) {
//        绑定mcid和qq返回结果
        VarInt packetId = new VarInt(0x28);
//        todo
        return null;
    }
}
