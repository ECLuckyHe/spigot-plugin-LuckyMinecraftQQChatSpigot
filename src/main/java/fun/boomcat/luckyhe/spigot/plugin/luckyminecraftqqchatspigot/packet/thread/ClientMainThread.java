package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.thread;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.ConfigOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarInt;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.pojo.Packet;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.ConnectionPacketReceiveUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.ConnectionPacketSendUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftFontStyleCode;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftMessageUtil;
import org.bukkit.Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

public class ClientMainThread extends Thread {

    private final Logger logger;
    private Socket socket;
    private Server server;

    private MinecraftConnectionThread minecraftThread;
    private boolean isRunning = true;

    public ClientMainThread(Server server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    public void close() {
        if (minecraftThread != null) {
            addSendQueue(ConnectionPacketSendUtil.getClosePacket("插件被卸载"));
        }
        isRunning = false;
    }

    public boolean isSocketClosed() {
        return socket.isClosed();
    }

    public void addSendQueue(Packet packet) {
        minecraftThread.addSendQueue(packet);
    }

    @Override
    public void run() {
        String host = ConfigOperation.getBotHost();
        int port = ConfigOperation.getBotPort();
        int retryTimes = ConfigOperation.getBotRetryTimes();

        while (isRunning) {
            try {
                socket = new Socket(host, port);
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();

                outputStream.write(ConnectionPacketSendUtil.getConnectPacket(
                        ConfigOperation.getBotSessionId(),
                        ConfigOperation.getServerName(),
                        ConfigOperation.getFormatJoin(),
                        ConfigOperation.getFormatQuit(),
                        ConfigOperation.getFormatMessage(),
                        ConfigOperation.getFormatDeath(),
                        ConfigOperation.getFormatKick()
                ).getBytes());

//                睡眠一秒，确保对方已收到
                Thread.sleep(1000);
                Packet packet = ConnectionPacketReceiveUtil.getPacket(inputStream);

                VarIntString sessionName = null;
                VarIntString address;
                VarInt heartbeatGap = null;

                switch (packet.getId().getValue()) {
                    case 0x00:
//                        连接正常
                        sessionName = new VarIntString(packet.getData());
                        address = new VarIntString(Arrays.copyOfRange(packet.getData(), sessionName.getBytesLength(), packet.getData().length));
                        heartbeatGap = new VarInt(Arrays.copyOfRange(packet.getData(), sessionName.getBytesLength() + address.getBytesLength(), packet.getData().length));
                        logger.info("连接成功，收到返回会话名：" + sessionName.getContent() + "，对方收到连接地址：" + address.getContent() + "，心跳包间隔：" + heartbeatGap.getValue() + "秒");
                        MinecraftMessageUtil.sendMinecraftMessage(
                                MinecraftFontStyleCode.LIGHT_PURPLE + "[LuckyChat] " +
                                        MinecraftFontStyleCode.GOLD + "已连接到会话 " + MinecraftFontStyleCode.GREEN + sessionName.getContent()
                        );
                        break;
                    case 0x01:
//                        连接异常
                        VarIntString errorMsg = new VarIntString(packet.getData());
                        logger.warning("连接失败，错误信息：" + errorMsg.getContent());
                        logger.warning("请修改配置文件后执行/reload指令");

                        MinecraftMessageUtil.sendMinecraftMessage(
                                MinecraftFontStyleCode.LIGHT_PURPLE + "[LuckyChat] " +
                                        MinecraftFontStyleCode.RED + "连接失败，错误信息：" + errorMsg.getContent()
                        );
                        MinecraftMessageUtil.sendMinecraftMessage(
                                MinecraftFontStyleCode.LIGHT_PURPLE + "[LuckyChat] " +
                                        MinecraftFontStyleCode.RED + "请修改配置文件后执行/mcchat reload指令"
                        );
                        isRunning = false;
                        socket.close();
                        continue;
                }


                minecraftThread = new MinecraftConnectionThread(server, socket, logger, sessionName.getContent(), heartbeatGap.getValue());
                minecraftThread.start();

//                循环直到线程结束
                while (minecraftThread.isAlive()) {

                }

                if (isRunning) {
                    logger.warning("连接已断开，" + retryTimes + "秒后再次尝试");
                    MinecraftMessageUtil.sendMinecraftMessage(
                            MinecraftFontStyleCode.LIGHT_PURPLE + "[LuckyChat] " +
                                    MinecraftFontStyleCode.RED + "连接已断开，稍后重新尝试连接"
                    );
                    Thread.sleep(1000L * retryTimes);
                }

            } catch (Exception e) {
                e.printStackTrace();
                logger.warning("Socket连接失败，" + retryTimes + "秒后再次尝试");
                try {
                    Thread.sleep(1000L * retryTimes);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        logger.info("连接线程关闭");
    }
}
