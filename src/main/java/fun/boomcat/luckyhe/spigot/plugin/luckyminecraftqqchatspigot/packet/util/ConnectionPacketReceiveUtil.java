package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.ConfigOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.QqOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarInt;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarLong;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.PacketLengthNotMatchException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarIntStringLengthNotMatchException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarIntTooBigException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarLongTooBigException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.pojo.Packet;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.FormatPlaceholder;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftMessageUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.ReplacePlaceholderUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ConnectionPacketReceiveUtil {
    public static Packet getPacket(InputStream inputStream) throws IOException, VarIntTooBigException, PacketLengthNotMatchException {
        VarInt packetLen = new VarInt(inputStream);
        VarInt packetId = new VarInt(inputStream);

        byte[] data = new byte[packetLen.getValue() - packetId.getBytesLength()];
        int readLen = inputStream.read(data);
        if (readLen != packetLen.getValue() - packetId.getBytesLength()) {
            throw new PacketLengthNotMatchException();
        }

        return new Packet(packetLen, packetId, data);
    }

    public static void handleMessageFromBot(String formatString, Packet packet, String sessionName) throws VarLongTooBigException, IOException, VarIntStringLengthNotMatchException, VarIntTooBigException, PacketLengthNotMatchException {
//        解析群消息的数据包
        byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getData().length);

        int index = 0;
        VarLong groupId = new VarLong(Arrays.copyOfRange(data, index, data.length));
        index += groupId.getBytesLength();
        VarIntString groupName = new VarIntString(Arrays.copyOfRange(data, index, data.length));
        index += groupName.getBytesLength();
        VarIntString groupNickname = new VarIntString(Arrays.copyOfRange(data, index, data.length));
        index += groupNickname.getBytesLength();
        VarLong senderId = new VarLong(Arrays.copyOfRange(data, index, data.length));
        index += senderId.getBytesLength();
        VarIntString senderNickname = new VarIntString(Arrays.copyOfRange(data, index, data.length));
        index += senderNickname.getBytesLength();
        VarIntString senderGroupNickname = new VarIntString(Arrays.copyOfRange(data, index, data.length));
        index += senderGroupNickname.getBytesLength();

        VarInt msgLength = new VarInt(Arrays.copyOfRange(data, index, data.length));
        index += msgLength.getBytesLength();

        List<TextComponent> textComponents = new ArrayList<>();
        List<Long> atList = new ArrayList<>();
        for (int i = 0; i < msgLength.getValue(); i++) {
            byte id = data[index];
            index++;

            switch (id) {
                case 0x00: {
//                    普通文本
                    VarIntString content = new VarIntString(Arrays.copyOfRange(data, index, data.length));
                    index += content.getBytesLength();

                    textComponents.add(new TextComponent(content.getContent()));
                    break;
                }
                case 0x01: {
//                    @
                    VarLong targetId = new VarLong(Arrays.copyOfRange(data, index, data.length));
                    index += targetId.getBytesLength();
                    VarIntString targetDisplayName = new VarIntString(Arrays.copyOfRange(data, index, data.length));
                    index += targetDisplayName.getBytesLength();

                    textComponents.add(new TextComponent("@" + targetId.getValue()));
                    atList.add(targetId.getValue());
                    break;
                }
                case 0x02: {
//                    @全体
                    textComponents.add(new TextComponent("@全体成员"));
                    textComponents.add(0, new TextComponent(ConfigOperation.getFormatFromBotMsgAtAll()));
                    break;
                }
                case 0x03:
                case 0x05: {
//                    图片地址
                    VarIntString url = new VarIntString(Arrays.copyOfRange(data, index, data.length));
                    index += url.getBytesLength();

                    TextComponent textComponent = new TextComponent();
                    if (id == 0x03) {
                        textComponent.setText(ConfigOperation.getFormatFromBotMsgPic());
                    }
                    if (id == 0x05) {
                        textComponent.setText(ConfigOperation.getFormatFromBotMsgAnimeFace());
                    }
                    textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url.getContent()));
                    textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                            new TextComponent("点击查看")
                    }));
                    textComponents.add(textComponent);
                    break;
                }
                case 0x04: {
//                    回复消息
                    VarLong fromId = new VarLong(Arrays.copyOfRange(data, index, data.length));
                    index += fromId.getBytesLength();
                    VarIntString oldMsg = new VarIntString(Arrays.copyOfRange(data, index, data.length));
                    index += oldMsg.getBytesLength();

                    TextComponent textComponent = new TextComponent(ConfigOperation.getFormatFromBotMsgQuoteReplyDisplay());
                    textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(
                            ReplacePlaceholderUtil.replacePlaceholderWithString(
                                    ConfigOperation.getFormatFromBotMsgQuoteReplyHover(),
                                    FormatPlaceholder.FROM_ID,
                                    String.valueOf(fromId.getValue()),
                                    FormatPlaceholder.OLD_MESSAGE,
                                    oldMsg.getContent()
                            )
                    )}));
                    textComponents.add(0, textComponent);
                    break;
                }
            }
        }

        if (index != data.length) {
            throw new PacketLengthNotMatchException();
        }

//        获取绑定id
        String mcIdByQq = QqOperation.getMcIdByQq(senderId.getValue());

//        随机数
        Random random = new Random();
        long randLong = random.nextLong();
        String res = ReplacePlaceholderUtil.replacePlaceholderWithString(
                formatString,
                FormatPlaceholder.GROUP_ID,
                String.valueOf(groupId.getValue()),
                FormatPlaceholder.GROUP_NAME,
                groupName.getContent(),
                FormatPlaceholder.GROUP_NICKNAME,
                groupNickname.getContent(),
                FormatPlaceholder.SENDER_ID,
                String.valueOf(senderId.getValue()),
                FormatPlaceholder.SENDER_NICKNAME,
                senderNickname.getContent(),
                FormatPlaceholder.SENDER_GROUP_NICKNAME,
                senderGroupNickname.getContent(),
                FormatPlaceholder.MESSAGE,
                FormatPlaceholder.MESSAGE + randLong,
                FormatPlaceholder.SESSION_NAME,
                sessionName,
                FormatPlaceholder.MC_ID,
                mcIdByQq != null ? mcIdByQq : senderGroupNickname.getContent()
        );

        //        主TextComponent
        TextComponent mainComponent = new TextComponent();

//        对控制台输出
        String[] split = res.split(FormatPlaceholder.MESSAGE + randLong, -1);
        for (int i = 0; i < split.length; i++) {
            mainComponent.addExtra(new TextComponent(split[i]));
            if (!(i == split.length - 1)) {
                for (TextComponent tc : textComponents) {
                    mainComponent.addExtra(tc);
                }
            }
        }
        MinecraftMessageUtil.logInfo(mainComponent.toPlainText());

        for (Player player : MinecraftMessageUtil.getOnlinePlayerList()) {
            List<Long> qqs = QqOperation.getQqsByMcid(player.getName());
            boolean contains = false;
            for (Long at : atList) {
                if (qqs.contains(at)) {
                    contains = true;
                    break;
                }
            }

            mainComponent = new TextComponent();
            String[] sp = res.split(FormatPlaceholder.MESSAGE + randLong, -1);
            for (int i = 0; i < sp.length; i++) {
                mainComponent.addExtra(new TextComponent(sp[i]));
                if (!(i == sp.length - 1)) {
                    mainComponent.addExtra(new TextComponent(contains ? ConfigOperation.getFormatFromBotMsgAtMe() : ""));
                    for (TextComponent tc : textComponents) {
                        mainComponent.addExtra(tc);
                    }
                }
            }

            player.spigot().sendMessage(mainComponent);
        }
    }
}
