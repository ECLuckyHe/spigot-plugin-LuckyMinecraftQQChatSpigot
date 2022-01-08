package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.pojo;


import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarInt;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.ByteUtil;

import java.util.Arrays;

public class Packet {
    private VarInt length;
    private VarInt id;
    private byte[] data;

    public VarInt getLength() {
        return length;
    }

    public void setLength(VarInt length) {
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Packet(VarInt length, VarInt id, byte[] data) {
        this.id = id;
        this.data = data;
        this.length = length;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "id=" + id +
                ", data=" + Arrays.toString(data) +
                ", length=" + length +
                '}';
    }

    public VarInt getId() {
        return id;
    }

    public void setId(VarInt id) {
        this.id = id;
    }

    public byte[] getBytes() {
        return ByteUtil.byteMergeAll(
                length.getBytes(),
                id.getBytes(),
                data
        );
    }
}