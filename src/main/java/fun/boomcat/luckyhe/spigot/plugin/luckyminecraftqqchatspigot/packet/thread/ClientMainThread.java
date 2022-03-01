package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.thread;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.ConfigOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarInt;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.pojo.Packet;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.ConnectionPacketReceiveUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.ConnectionPacketSendUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.FormatPlaceholder;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.MinecraftMessageUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.ReplacePlaceholderUtil;
import org.bukkit.Server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class ClientMainThread extends Thread {

    private final Logger logger;
    private Socket socket;
    private Server server;

    private MinecraftConnectionThread minecraftThread;
    private boolean isRunning = true;

    private CountDownLatch cdl;

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
            cdl = new CountDownLatch(1);
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
                        ConfigOperation.getFormatKick(),
                        ConfigOperation.getOnlinePlayersCommands(),
                        ConfigOperation.getOnlinePlayersResponseFormat(),
                        ConfigOperation.getOnlinePlayersResponseSeparator(),
                        ConfigOperation.getRconCommandPrefix(),
                        ConfigOperation.getRconCommandResultFormat(),
                        ConfigOperation.getRconCommandUserPrefix(),
                        ConfigOperation.getRconCommandUserBindPrefix()
                ).getBytes());

//                睡眠一秒，确保对方已收到
                Thread.sleep(1000L);
                Packet packet = ConnectionPacketReceiveUtil.getPacket(inputStream);

                VarIntString sessionName = null;
                VarIntString address = null;
                VarInt heartbeatInterval = null;

                switch (packet.getId().getValue()) {
                    case 0x00:
//                        连接正常
                        sessionName = new VarIntString(packet.getData());
                        address = new VarIntString(Arrays.copyOfRange(packet.getData(), sessionName.getBytesLength(), packet.getData().length));
                        heartbeatInterval = new VarInt(Arrays.copyOfRange(packet.getData(), sessionName.getBytesLength() + address.getBytesLength(), packet.getData().length));
                        logger.info("连接成功，收到返回会话名：" + sessionName.getContent() + "，对方收到连接地址：" + address.getContent() + "，心跳包间隔：" + heartbeatInterval.getValue() + "秒");

                        MinecraftMessageUtil.sendMinecraftMessage(ReplacePlaceholderUtil.replacePlaceholderWithString(
                                ConfigOperation.getInfoOnConnected(),
                                FormatPlaceholder.SERVER_NAME,
                                ConfigOperation.getServerName(),
                                FormatPlaceholder.SESSION_ID,
                                String.valueOf(ConfigOperation.getBotSessionId()),
                                FormatPlaceholder.SESSION_NAME,
                                sessionName.getContent(),
                                FormatPlaceholder.PING_INTERVAL,
                                String.valueOf(heartbeatInterval.getValue()),
                                FormatPlaceholder.REMOTE_ADDRESS,
                                address.getContent()
                        ));

                        break;
                    case 0x01:
//                        连接异常
                        VarIntString errorMsg = new VarIntString(packet.getData());
                        logger.warning("连接失败，错误信息：" + errorMsg.getContent());
                        logger.warning("请修改配置文件后执行/reload指令");

                        MinecraftMessageUtil.sendMinecraftMessage(ReplacePlaceholderUtil.replacePlaceholderWithString(
                                ConfigOperation.getInfoOnRequestError(),
                                FormatPlaceholder.SERVER_NAME,
                                ConfigOperation.getServerName(),
                                FormatPlaceholder.ERROR_MESSAGE,
                                errorMsg.getContent()
                        ));
                        isRunning = false;
                        socket.close();
                        continue;
                }


                minecraftThread = new MinecraftConnectionThread(server, socket, logger, sessionName.getContent(), heartbeatInterval.getValue(), address.getContent(), cdl);
                minecraftThread.start();

                cdl.await();

                if (isRunning) {
                    logger.warning("连接已断开，" + retryTimes + "秒后再次尝试");
                    MinecraftMessageUtil.sendMinecraftMessage(ReplacePlaceholderUtil.replacePlaceholderWithString(
                            ConfigOperation.getInfoOnConnectionDisconnect(),
                            FormatPlaceholder.SERVER_NAME,
                            ConfigOperation.getServerName(),
                            FormatPlaceholder.SESSION_ID,
                            String.valueOf(minecraftThread.getSessionId()),
                            FormatPlaceholder.SESSION_NAME,
                            minecraftThread.getSessionName(),
                            FormatPlaceholder.PING_INTERVAL,
                            String.valueOf(minecraftThread.getHeartbeatInterval()),
                            FormatPlaceholder.REMOTE_ADDRESS,
                            minecraftThread.getRemoteAddress()
                    ));

                    for (int i = 0; i < retryTimes && isRunning; i++) {
//                        防止等待过长
                        Thread.sleep(1000L);
                    }
                }

            } catch (Exception e) {
//                e.printStackTrace();
                logger.warning("Socket连接失败，" + retryTimes + "秒后再次尝试");
                try {
                    for (int i = 0; i < retryTimes && isRunning; i++) {
//                        防止等待过长
                        Thread.sleep(1000L);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        logger.info("连接线程关闭");
    }
}
