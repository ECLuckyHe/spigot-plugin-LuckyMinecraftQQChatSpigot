package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.thread;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.ConfigOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.pojo.Packet;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.ConnectionPacketReceiveUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.ConnectionPacketSendUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

public class ClientMainThread extends Thread {

    private final Logger logger;
    private Socket socket;

    private MinecraftConnectionThread minecraftThread;
    private boolean isRunning = true;

    public ClientMainThread(Logger logger) {
        this.logger = logger;
    }

    public void close() {
        addSendQueue(ConnectionPacketSendUtil.getClosePacket("插件被卸载"));
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

                switch (packet.getId().getValue()) {
                    case 0x00:
//                        连接正常
                        sessionName = new VarIntString(packet.getData());
                        address = new VarIntString(Arrays.copyOfRange(packet.getData(), sessionName.getBytesLength(), packet.getData().length));
                        logger.info("连接成功，收到返回会话名：" + sessionName.getContent());
                        break;
                    case 0x01:
//                        连接异常
                        VarIntString errorMsg = new VarIntString(packet.getData());
                        logger.warning("连接失败，错误信息：" + errorMsg.getContent());
                        logger.warning("将在" + retryTimes + "秒后继续尝试");

                        try {
                            Thread.sleep(1000L * retryTimes);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }

                        socket.close();

                        continue;
                }


                minecraftThread = new MinecraftConnectionThread(socket, logger, sessionName.getContent());
                minecraftThread.start();

                while (!minecraftThread.isStop()) {}
            } catch (Exception e) {
                e.printStackTrace();
                logger.warning("Socket连接失败，" + retryTimes + "秒后再次尝试");
                try {
                    Thread.sleep(1000L * retryTimes);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                continue;
            }
        }

        logger.info("连接线程关闭");
    }
}
