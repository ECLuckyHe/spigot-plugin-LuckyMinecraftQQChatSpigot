package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.ConfigOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.DataOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.QqOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.SendBindMessageToPlayerFailException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.UserCommandConflictException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.UserCommandExistException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.UserCommandNotExistException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarInt;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarLong;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarIntStringLengthNotMatchException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarIntTooBigException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.pojo.Packet;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.*;
import org.bukkit.entity.Player;

import java.io.FileNotFoundException;
import java.io.IOException;
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
            String userBindPrefix,
            List<String> getUserCommandsCommands
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

        VarInt guccc = new VarInt(getUserCommandsCommands.size());
        VarIntString[] guccs = new VarIntString[guccc.getValue()];

        for (int i = 0; i < guccs.length; i++) {
            guccs[i] = new VarIntString(getUserCommandsCommands.get(i));
        }

        int totalLengthInt = packetId.getBytesLength() + si.getBytesLength() + sn.getBytesLength() +
                jfs.getBytesLength() + qfs.getBytesLength() + mfs.getBytesLength() + dfs.getBytesLength() +
                kfs.getBytesLength() + opcc.getBytesLength() + opcrf.getBytesLength() + opcrs.getBytesLength() +
                rcp.getBytesLength() + rcrf.getBytesLength() + ucp.getBytesLength() + ubp.getBytesLength() +
                guccc.getBytesLength();
        for (VarIntString opcca : opccs) {
            totalLengthInt += opcca.getBytesLength();
        }
        for (VarIntString gucc : guccs) {
            totalLengthInt += gucc.getBytesLength();
        }

//        ??????????????????????????????????????????

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
                ubp.getBytes(),
                guccc.getBytes()
        );
        for (VarIntString gucc : guccs) {
            data = ByteUtil.byteMergeAll(data, gucc.getBytes());
        }

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
            commandResult = new VarIntString("MC??????????????????RCON?????????????????????????????????");
        } else {
            commandResult = new VarIntString("???????????????");
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
            commandResult = new VarIntString("MC??????????????????RCON?????????????????????????????????");
        } else {
            if (command.contains("reload")) {
                commandResult = new VarIntString("??????????????????reload?????????");
            } else {
//                ????????????
                try {
                    commandResult = new VarIntString(RconUtil.sendMcCommad(command));
                } catch (Exception e) {
                    e.printStackTrace();
                    commandResult = new VarIntString("??????????????????");
                }
            }
        }

        return new Packet(
                new VarInt(packetId.getBytesLength() + commandResult.getBytesLength()),
                packetId,
                commandResult.getBytes()
        );
    }

    public static Packet getUserCommandResultPacket(boolean rconEnable, long senderId, String content) {
//        ????????????????????????
        VarInt packetId = new VarInt(0x24);
        VarIntString commandResult;
        if (!rconEnable) {
//            ?????????rcon
            commandResult = new VarIntString("MC??????????????????RCON???????????????????????????????????????");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + commandResult.getBytesLength()),
                    packetId,
                    commandResult.getBytes()
            );
        }

        String mcid;
        try {
            mcid = QqOperation.getMcIdByQq(senderId);
        } catch (Exception e) {
            e.printStackTrace();
            commandResult = new VarIntString("????????????????????????????????????????????????");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + commandResult.getBytesLength()),
                    packetId,
                    commandResult.getBytes()
            );
        }

        if (mcid == null) {
            String userBindPrefix = ReplacePlaceholderUtil.replacePlaceholderWithString(
                    ConfigOperation.getRconCommandUserBindPrefix(),
                    FormatPlaceholder.SERVER_NAME,
                    ConfigOperation.getServerName()
            );

            commandResult = new VarIntString("?????????\n" +
                    "" + userBindPrefix + "??????mc???id\n" +
                    "??????????????????????????????????????????");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + commandResult.getBytesLength()),
                    packetId,
                    commandResult.getBytes()
            );
        }

//        ??????????????????
        String realCommand;
        try {
            realCommand = UserCommandUtil.getRealCommandByContent(
                    DataOperation.getRconCommandUserCommands(),
                    content
            );
        } catch (Exception e) {
            e.printStackTrace();
            commandResult = new VarIntString("????????????????????????????????????????????????");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + commandResult.getBytesLength()),
                    packetId,
                    commandResult.getBytes()
            );
        }

//        ???????????????
        if (realCommand == null) {
            commandResult = new VarIntString("??????????????????");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + commandResult.getBytesLength()),
                    packetId,
                    commandResult.getBytes()
            );
        }

//        ????????????????????????
        String realCommandWithPlayer = ReplacePlaceholderUtil.replacePlaceholderWithString(
                realCommand,
                FormatPlaceholder.PLAYER_NAME,
                mcid
        );
        try {
            commandResult = new VarIntString("/" + realCommandWithPlayer + "\n\n" + RconUtil.sendMcCommad(realCommandWithPlayer));
        } catch (Exception e) {
            e.printStackTrace();
            commandResult = new VarIntString("??????????????????");
        }

        return new Packet(
                new VarInt(packetId.getBytesLength() + commandResult.getBytesLength()),
                packetId,
                commandResult.getBytes()
        );
    }

    public static Packet getAddUserCommandResultPacket(long id, String name, String userCommand, String mapCommand) {
//        ??????????????????????????????
        VarInt packetId = new VarInt(0x25);

//        op??????
        boolean rconCommandOpIdExist = false;
        try {
            rconCommandOpIdExist = DataOperation.isRconCommandOpIdExist(id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (!rconCommandOpIdExist) {
            VarIntString res = new VarIntString("?????? " + id + " ??????MC?????????op?????????MC???????????????????????????op?????????\n" +
                    "/mcchat addop " + id);
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

//        ????????????
        Map<String, String> commandMap;
        try {
            commandMap = DataOperation.getRconCommandUserCommandByName(name);
        } catch (Exception e) {
            VarIntString res = new VarIntString("??????????????????????????????");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

//        ????????????????????????????????????????????????????????????????????????????????????
        if (commandMap != null) {
            String n = commandMap.get("name");
            String c = commandMap.get("command");
            String m = commandMap.get("mapping");
            VarIntString res = new VarIntString("???????????? " + name + " ????????????\n" +
                    "????????????" + n + "\n" +
                    "???????????????" + c + "\n" +
                    "???????????????" + m);
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

//        ??????????????????????????????
        userCommand = UserCommandUtil.craftCommand(UserCommandUtil.splitCommand(userCommand));
        mapCommand = UserCommandUtil.craftCommand(UserCommandUtil.splitCommand(mapCommand));
        List<String> userCommandStrings = UserCommandUtil.getCommandArgList(userCommand);
        List<String> mapCommandStrings = UserCommandUtil.getCommandArgList(mapCommand);

        for (String mapCommandString : mapCommandStrings) {
            userCommandStrings.removeIf(o -> o.equals(mapCommandString));
        }

//        ?????????????????????????????????
        if (userCommandStrings.size() != 0) {
            StringBuilder sb = new StringBuilder("???????????????????????????????????????????????????\n");
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

//        ????????????????????????????????????????????????
        userCommandStrings = UserCommandUtil.getCommandArgList(userCommand);
        mapCommandStrings = UserCommandUtil.getCommandArgList(mapCommand);

        for (String userCommandString : userCommandStrings) {
            mapCommandStrings.removeIf(o -> o.equals(userCommandString));
        }

        if (mapCommandStrings.size() != 0) {
            StringBuilder sb = new StringBuilder("???????????????????????????????????????????????????\n");
            for (String s : mapCommandStrings) {
                sb.append(s).append("\n");
            }
            VarIntString res = new VarIntString(sb.toString());
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

//        ????????????
        try {
            DataOperation.addRconCommandUserCommand(name, userCommand, mapCommand);
        } catch (UserCommandExistException e) {
            e.printStackTrace();
        } catch (UserCommandConflictException e) {
            VarIntString res = new VarIntString("????????????????????????????????????????????????\n" +
                    "????????????" + e.getName() + "\n" +
                    "???????????????" + e.getCommand() + "\n" +
                    "???????????????" + e.getMapping());
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        } catch (Exception e) {
            e.printStackTrace();
            VarIntString res = new VarIntString("??????????????????????????????");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

//        ???????????????
        try {
            commandMap = DataOperation.getRconCommandUserCommandByName(name);
        } catch (Exception e) {
            VarIntString res = new VarIntString("??????????????????????????????");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

        String n = commandMap.get("name");
        String c = commandMap.get("command");
        String m = commandMap.get("mapping");
        VarIntString res = new VarIntString("???????????? " + name + " ????????????\n" +
                "????????????" + n + "\n" +
                "???????????????" + c + "\n" +
                "???????????????" + m);
        return new Packet(
                new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                packetId,
                res.getBytes()
        );
    }

    public static Packet getDelUserCommandResultPacket(long id, String name) {
//        ????????????????????????
        VarInt packetId = new VarInt(0x26);

        //        op??????
        boolean rconCommandOpIdExist = false;
        try {
            rconCommandOpIdExist = DataOperation.isRconCommandOpIdExist(id);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (!rconCommandOpIdExist) {
            VarIntString res = new VarIntString("?????? " + id + " ??????MC?????????op?????????MC???????????????????????????op?????????\n" +
                    "/mcchat addop " + id);
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

//        ????????????
        Map<String, String> commandMap = null;
        try {
            commandMap = DataOperation.getRconCommandUserCommandByName(name);
        } catch (Exception e) {
            VarIntString res = new VarIntString("??????????????????????????????");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

//        ?????????????????????????????????
        if (commandMap == null) {
            VarIntString res = new VarIntString("?????????????????????" + name);
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

//        ????????????
        try {
            DataOperation.delRconCommandUserCommand(name);
        } catch (UserCommandNotExistException e) {
            e.printStackTrace();
        } catch (Exception e) {
            VarIntString res = new VarIntString("??????????????????????????????");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

        boolean isExist;
        try {
            isExist = DataOperation.isRconCommandUserCommandNameExist(name);
        } catch (Exception e) {
            VarIntString res = new VarIntString("??????????????????????????????");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

        if (isExist) {
            VarIntString res = new VarIntString("??????????????????????????????");
            return new Packet(
                    new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                    packetId,
                    res.getBytes()
            );
        }

        String n = commandMap.get("name");
        String c = commandMap.get("command");
        String m = commandMap.get("mapping");
        VarIntString res = new VarIntString("????????????" + name + "????????????\n" +
                "????????????" + n + "\n" +
                "???????????????" + c + "\n" +
                "???????????????" + m);
        return new Packet(
                new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                packetId,
                res.getBytes()
        );
    }

    public static Packet getMcChatUserCommandResultPacket(int packetId) throws FileNotFoundException {
//        ???????????????????????????????????????????????????????????????????????????????????????id
        VarInt pid = new VarInt(packetId);
        List<Map<String, String>> rconCommandUserCommands = DataOperation.getRconCommandUserCommands();
        VarInt commandLength = new VarInt(rconCommandUserCommands.size());

        int packetLength = pid.getBytesLength() + commandLength.getBytesLength();
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
                pid,
                data
        );
    }

    public static Packet getUserBindResultPacket(long senderId, String mcid) {
//        ??????mcid???qq????????????
        VarInt packetId = new VarInt(0x28);

        String mcMessage = MinecraftFontStyleCode.GOLD + "QQ??????" + MinecraftFontStyleCode.GREEN + senderId + MinecraftFontStyleCode.GOLD + "?????????????????????MCID " + MinecraftFontStyleCode.GREEN + mcid + MinecraftFontStyleCode.GOLD +
                " ??????\n" +
                "??????/qq confirm " + senderId + " ????????????\n" +
                "??????/qq confirm " + senderId + " ????????????";
        VarIntString res;
        try {
            MinecraftMessageUtil.sendMessageToPlayer(mcid, mcMessage);
            BindQqUtil.addBind(senderId, mcid);
            res = new VarIntString("????????????" + mcid + "??????????????????????????????MC?????????" +
                    "/qq confirm " + senderId + " ???????????????");
        } catch (SendBindMessageToPlayerFailException e) {
            res = new VarIntString("??????" + mcid + "????????????????????????");
        }

        return new Packet(
                new VarInt(packetId.getBytesLength() + res.getBytesLength()),
                packetId,
                res.getBytes()
        );
    }
}
