package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarLongTooBigException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class VarLong {
    private final long value;

    @Override
    public String toString() {
        return "VarLong{" +
                "value=" + value +
                '}';
    }

    public byte[] getBytes() {
        long value = this.value;
        byte[] bytes = new byte[10];
        int i = 0;

        while (true) {
            if ((value & ~0x7F) == 0) {
                bytes[i] = ((byte) value);
                return Arrays.copyOfRange(bytes, 0, i + 1);
            }

            bytes[i] = ((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
            i++;
        }
    }

    public int getBytesLength() {
        return getBytes().length;
    }

    public long getValue() {
        return this.value;
    }

    public VarLong(InputStream inputStream) throws IOException, VarLongTooBigException {
        long value = 0;
        int length = 0;
        byte currentByte;

        do {
            currentByte = ((byte) inputStream.read());
            value |= (((long)currentByte) & 0x7F) << (length * 7);
            length++;

            if (length > 10) {
                throw new VarLongTooBigException();
            }
        } while ((currentByte & 0x80) == 0x80);

        this.value = value;
    }

    public VarLong(byte[] bytes) throws IOException, VarLongTooBigException {
        this(new ByteArrayInputStream(bytes));
    }

    public VarLong(long value) {
        this.value = value;
    }
}
