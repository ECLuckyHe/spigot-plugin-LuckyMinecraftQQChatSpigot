package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarIntStringLengthNotMatchException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.exception.VarIntTooBigException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.ByteUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class VarIntString {
    private String content;
    private Charset charset;
    private byte[] bytes;
    private int len = -1;

    @Override
    public String toString() {
        return "VarIntString{" +
                "content='" + content + '\'' +
                ", charset=" + charset +
                ", bytes=" + Arrays.toString(bytes) +
                ", len=" + len +
                '}';
    }

    public VarIntString(String content, Charset charset) {
        this.content = content;
        this.charset = charset;
    }

    public VarIntString(String content) {
        this(content, StandardCharsets.UTF_8);
    }

    public VarIntString(InputStream inputStream, Charset charset) throws IOException, VarIntTooBigException, VarIntStringLengthNotMatchException {
        VarInt len = new VarInt(inputStream);
        byte[] bytes = new byte[len.getValue()];
        int readLen = inputStream.read(bytes);
        if (readLen == -1) {
//            20220502添加
//            此处若VarIntString类型变量在包最后一项且len刚好为0时，无下一个字节
//            因此此处会返回-1
            readLen = 0;
        }
        if (readLen != len.getValue()) {
            throw new VarIntStringLengthNotMatchException();
        }

        this.content = new String(bytes, charset);
    }

    public VarIntString(InputStream inputStream) throws VarIntStringLengthNotMatchException, IOException, VarIntTooBigException {
        this(inputStream, StandardCharsets.UTF_8);
    }

    public VarIntString(byte[] bytes, Charset charset) throws VarIntStringLengthNotMatchException, IOException, VarIntTooBigException {
        this(new ByteArrayInputStream(bytes), charset);
    }

    public VarIntString(byte[] bytes) throws VarIntStringLengthNotMatchException, IOException, VarIntTooBigException {
        this(bytes, StandardCharsets.UTF_8);
    }

    public String getContent() {
        return this.content;
    }

    public int getContentLength() {
        return this.content.length();
    }

    public int getBytesLength(Charset charset) {
        if (len == -1) {
            len = getBytes(charset).length;
        }
        return len;
    }

    public int getBytesLength() {
        return getBytesLength(StandardCharsets.UTF_8);
    }

    public byte[] getBytes(Charset charset) {
        if (this.bytes != null) {
            return this.bytes;
        }
        byte[] bytes = content.getBytes(charset);
        byte[] lengthBytes = (new VarInt(bytes.length)).getBytes();
        this.bytes =  ByteUtil.byteMergeAll(lengthBytes, bytes);
        return this.bytes;
    }

    public byte[] getBytes() {
        return getBytes(StandardCharsets.UTF_8);
    }
}
