package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.QqOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarInt;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarLong;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.PacketLengthNotMatchException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarIntStringLengthNotMatchException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarIntTooBigException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarLongTooBigException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.pojo.Packet;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftMessageUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.FormatPlaceholder;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.ReplacePlaceholderUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

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
        VarIntString message = new VarIntString(Arrays.copyOfRange(data, index, data.length));
        index += message.getBytesLength();

        if (index != data.length) {
            throw new PacketLengthNotMatchException();
        }

//        获取绑定id
        String mcIdByQq = QqOperation.getMcIdByQq(senderId.getValue());

        MinecraftMessageUtil.sendMinecraftMessage(ReplacePlaceholderUtil.replacePlaceholderWithString(
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
                message.getContent(),
                FormatPlaceholder.SESSION_NAME,
                sessionName,
                FormatPlaceholder.MC_ID,
                mcIdByQq != null ? mcIdByQq : senderGroupNickname.getContent()
                ));
    }
}
