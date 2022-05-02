package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarLongTooBigException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class VarLong {
    private final long value;
    private byte[] bytes;
    private int len = -1;

    @Override
    public String toString() {
        return "VarLong{" +
                "value=" + value +
                ", bytes=" + Arrays.toString(bytes) +
                ", len=" + len +
                '}';
    }

    public byte[] getBytes() {
        if (this.bytes != null) {
            return this.bytes;
        }
        long value = this.value;
        byte[] bytes = new byte[10];
        int i = 0;

        while (true) {
            if ((value & ~0x7F) == 0) {
                bytes[i] = ((byte) value);
                this.bytes =  Arrays.copyOfRange(bytes, 0, i + 1);
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
