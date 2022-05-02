package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarIntTooBigException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class VarInt {
    private final int value;
    private byte[] bytes;
    private int len = -1;

    public byte[] getBytes() {
        if (this.bytes != null) {
            return this.bytes;
        }
        int value = this.value;
        byte[] bytes = new byte[5];
        int i = 0;

        while (true) {
            if ((value & ~0x7F) == 0) {
                bytes[i] = ((byte) value);
                this.bytes = Arrays.copyOfRange(bytes, 0, i + 1);
                break;
            }

            bytes[i] = ((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
            i++;
        }
        return this.bytes;
    }

    public int getBytesLength() {
        if (len == -1) {
            len = getBytes().length;
        }
        return len;
    }

    public int getValue() {
        return this.value;
    }

    public VarInt(InputStream inputStream) throws VarIntTooBigException, IOException {
        int value = 0;
        int length = 0;
        byte currentByte;

        do {
            currentByte = ((byte) inputStream.read());
            value |= (currentByte & 0x7F) << (length * 7);
            length++;

            if (length > 5) {
                throw new VarIntTooBigException();
            }
        } while ((currentByte & 0x80) == 0x80);

        this.value = value;
    }

    public VarInt(byte[] bytes) throws IOException, VarIntTooBigException {
        this(new ByteArrayInputStream(bytes));
    }

    public VarInt(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "VarInt{" +
                "value=" + value +
                ", bytes=" + Arrays.toString(bytes) +
                ", len=" + len +
                '}';
    }
}
