package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.thread;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.ConfigOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.DataOperation;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.UserCommandExistException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarInt;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.datatype.VarLong;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.pojo.Packet;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.ConnectionPacketReceiveUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.ConnectionPacketSendUtil;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.*;
import org.bukkit.Server;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class MinecraftConnectionThread extends Thread {
    private final String serverName = ConfigOperation.getServerName();
    private final long sessionId = ConfigOperation.getBotSessionId();
    private final String sessionName;
    private final int heartbeatInterval;
    private int heartbeatCount = 0;
    private final String remoteAddress;

    private final boolean rconEnable = ConfigOperation.getRconCommandEnabled();

    private final LinkedBlockingQueue<Packet> sendQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Packet> receiveQueue = new LinkedBlockingQueue<>();

    private final Server server;
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    private final Logger logger;

    private final String formatFromBot = ConfigOperation.getFormatFromBot();

    private boolean isConnected = true;
    private boolean isStop = false;
    private final CountDownLatch allThreadCdl = new CountDownLatch(4);

    //    当 不取队列的线程 运行完成时减一
    private final CountDownLatch noTakeQueueThreadCdl = new CountDownLatch(2);
    private final CountDownLatch mainThreadCdl;

    private void logInfo(String threadName, String info) {
        logger.info("[" + threadName + "] " + info);
    }

    private void logWarning(String threadName, String warning) {
        logger.info("[" + threadName + "] " + warning);
    }

    public void sendClosePacket(String info) {
        VarInt packetId = new VarInt(0xF0);
        VarIntString string = new VarIntString(info);
        sendQueue.add(new Packet(new VarInt(packetId.getBytesLength() + string.getBytesLength()), packetId, string.getBytes()));
    }

    public void addSendQueue(Packet packet) {
        sendQueue.add(packet);
    }

    public boolean isStop() {
        return isStop;
    }

    public long getSessionId() {
        return sessionId;
    }

    public String getSessionName() {
        return sessionName;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public void run() {
        AsyncCaller.run(() -> {
//            此内容与bot端的发送线程差别不大
            String threadName = "发送线程";
            logInfo(threadName, "线程启动");

            while (isConnected) {
                try {
                    Packet packet = sendQueue.take();
                    outputStream.write(packet.getBytes());
                    outputStream.flush();

//                        如果包id为-1，不发出且进行下一轮循环
                    if (packet.getId().getValue() == -1) {
                        continue;
                    }

                    if (packet.getId().getValue() == 0xF0) {
                        logInfo(threadName, "发送关闭包，内容" + new VarIntString(packet.getData()).getContent());
                        isConnected = false;
                        socket.close();
                    }

                } catch (Exception e) {
                    isConnected = false;
                    e.printStackTrace();
                    logWarning(threadName, "线程出现异常，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logWarning(threadName, "Socket关闭失败");
                    }
                }
            }

            allThreadCdl.countDown();
            logInfo(threadName, "结束工作");
        });

        AsyncCaller.run(() -> {
            String threadName = "接收线程";

            while (isConnected) {
                try {
                    Packet packet = ConnectionPacketReceiveUtil.getPacket(inputStream);
                    receiveQueue.add(packet);
                } catch (Exception e) {
                    isConnected = false;
                    e.printStackTrace();
                    logWarning(threadName, "线程出现异常，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logWarning(threadName, "Socket关闭失败");
                    }
                }
            }

            allThreadCdl.countDown();
            noTakeQueueThreadCdl.countDown();
            logInfo(threadName, "结束工作");
        });

        AsyncCaller.run(() -> {
            String threadName = "接收处理";
            logInfo(threadName, "线程启动");

            while (isConnected) {
                try {
                    Packet packet = receiveQueue.take();
                    switch (packet.getId().getValue()) {
                        case -1:
                            continue;

                        case 0x20:
//                                回应心跳包
                            VarLong ping = new VarLong(packet.getData());
                            sendQueue.add(ConnectionPacketSendUtil.getPongPacket(ping.getValue()));
                            heartbeatCount = 0;
                            break;

                        case 0x21:
//                                收到要求提供玩家在线信息数据的数据包
                            sendQueue.add(ConnectionPacketSendUtil.getOnlinePlayersPacket());
                            break;

                        case 0x22: {
//                                指令
                            int commandIndex = 0;
                            VarLong commandSenderId = new VarLong(packet.getData());
                            commandIndex += commandSenderId.getBytesLength();
                            VarIntString command = new VarIntString(Arrays.copyOfRange(packet.getData(), commandIndex, packet.getData().length));


                            if (!DataOperation.isRconCommandOpIdExist(commandSenderId.getValue())) {
                                sendQueue.add(ConnectionPacketSendUtil.getRconCommandRefusedPacket(rconEnable));
                                break;
                            }

                            sendQueue.add(ConnectionPacketSendUtil.getRconCommandResultPacket(
                                    rconEnable,
                                    command.getContent()
                            ));
                            break;
                        }

                        case 0x23: {
//                                公告
                            int i = 0;
                            VarLong announcementSenderId = new VarLong(packet.getData());
                            i += announcementSenderId.getBytesLength();
                            VarIntString announcementSenderNickname = new VarIntString(Arrays.copyOfRange(packet.getData(), i, packet.getData().length));
                            i += announcementSenderNickname.getBytesLength();
                            VarIntString announcement = new VarIntString(Arrays.copyOfRange(packet.getData(), i, packet.getData().length));

                            String announcementToBeSent = ReplacePlaceholderUtil.replacePlaceholderWithString(
                                    ConfigOperation.getAnnouncementFormat(),
                                    FormatPlaceholder.SERVER_NAME,
                                    serverName,
                                    FormatPlaceholder.SENDER_ID,
                                    String.valueOf(announcementSenderId.getValue()),
                                    FormatPlaceholder.SENDER_NICKNAME,
                                    String.valueOf(announcementSenderNickname.getContent()),
                                    FormatPlaceholder.ANNOUNCEMENT,
                                    announcement.getContent()
                            );

                            MinecraftMessageUtil.sendMinecraftMessage(announcementToBeSent);
                            break;
                        }

                        case 0x24: {
//                            发送用户指令
                            byte[] data = packet.getData();
                            int i = 0;
                            VarLong qq = new VarLong(Arrays.copyOfRange(data, i, data.length));
                            i += qq.getBytesLength();
                            VarIntString content = new VarIntString(Arrays.copyOfRange(data, i, data.length));

                            addSendQueue(ConnectionPacketSendUtil.getUserCommandResultPacket(
                                    rconEnable,
                                    qq.getValue(),
                                    content.getContent()
                            ));

                            break;
                        }

                        case 0x25: {
//                            添加用户指令
                            int i = 0;
                            byte[] data = packet.getData();
                            VarLong senderId = new VarLong(Arrays.copyOfRange(data, i, data.length));
                            i += senderId.getBytesLength();
                            VarIntString name = new VarIntString(Arrays.copyOfRange(data, i, data.length));
                            i += name.getBytesLength();
                            VarIntString userCommand = new VarIntString(Arrays.copyOfRange(data, i, data.length));
                            i += userCommand.getBytesLength();
                            VarIntString mapCommand = new VarIntString(Arrays.copyOfRange(data, i, data.length));

                            sendQueue.add(ConnectionPacketSendUtil.getAddUserCommandResultPacket(
                                    senderId.getValue(),
                                    name.getContent(),
                                    userCommand.getContent(),
                                    mapCommand.getContent()
                            ));
                            break;
                        }

                        case 0x26: {
//                            删除用户指令
                            int i = 0;
                            byte[] data = packet.getData();
                            VarLong senderId = new VarLong(Arrays.copyOfRange(data, i, data.length));
                            i += senderId.getBytesLength();
                            VarIntString commandName = new VarIntString(Arrays.copyOfRange(data, i, data.length));

                            sendQueue.add(ConnectionPacketSendUtil.getDelUserCommandResultPacket(
                                    senderId.getValue(),
                                    commandName.getContent()
                            ));
                            break;
                        }

                        case 0x27: {
//                            获取用户指令列表（mcchat指令）
                            sendQueue.add(ConnectionPacketSendUtil.getMcChatUserCommandResultPacket(0x27));
                            break;
                        }

                        case 0x29: {
//                            获取用户指令列表（普通用户）
                            sendQueue.add(ConnectionPacketSendUtil.getMcChatUserCommandResultPacket(0x29));
                            break;
                        }

                        case 0x28: {
//                            绑定mcid和qq返回
                            int i = 0;
                            byte[] data = packet.getData();
                            VarLong qq = new VarLong(Arrays.copyOfRange(data, i, data.length));
                            i += qq.getBytesLength();
                            VarIntString mcid = new VarIntString(Arrays.copyOfRange(data, i, data.length));

                            sendQueue.add(ConnectionPacketSendUtil.getUserBindResultPacket(qq.getValue(), mcid.getContent()));
                            break;
                        }

                        case 0xF0: {
//                                关闭包
                            VarIntString exitMsg = new VarIntString(packet.getData());
                            logInfo(threadName, "对方要求断开连接，原因：" + exitMsg.getContent());

                            MinecraftMessageUtil.sendMinecraftMessage(ReplacePlaceholderUtil.replacePlaceholderWithString(
                                    ConfigOperation.getInfoOnBotRequestClose(),
                                    FormatPlaceholder.SERVER_NAME,
                                    ConfigOperation.getServerName(),
                                    FormatPlaceholder.SESSION_ID,
                                    String.valueOf(sessionId),
                                    FormatPlaceholder.SERVER_NAME,
                                    sessionName,
                                    FormatPlaceholder.PING_INTERVAL,
                                    String.valueOf(heartbeatInterval),
                                    FormatPlaceholder.REMOTE_ADDRESS,
                                    remoteAddress,
                                    FormatPlaceholder.REASON,
                                    exitMsg.getContent()
                            ));

                            isConnected = false;
                            socket.close();
                            break;
                        }

                        case 0x11: {
//                                原封不动发送到服内的消息包
                            VarIntString msg = new VarIntString(packet.getData());
                            MinecraftMessageUtil.sendMinecraftMessage(msg.getContent());
                            break;
                        }

                        case 0x10: {
//                                来自群内的消息
                            ConnectionPacketReceiveUtil.handleMessageFromBot(formatFromBot, packet, sessionName);
                            break;
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    isConnected = false;
                    logWarning(threadName, "出现异常，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logWarning(threadName, "Socket关闭失败");
                    }
                }
            }

            allThreadCdl.countDown();
            logInfo(threadName, "结束工作");
        });

        AsyncCaller.run(() -> {
            String threadName = "心跳检测";
            logInfo(threadName, "线程启动");

            while (isConnected) {
                try {
                    heartbeatCount += 1;
                    Thread.sleep(1000L);

//                    如果超过心跳包发送频率+5秒以上，则断开连接
                    if (3 * heartbeatInterval + 5 < heartbeatCount) {
                        logWarning(threadName, "已经" + heartbeatCount + "秒未接收到心跳包，开始关闭连接（服务端发送心跳包频率为" + heartbeatInterval + "秒）");
                        MinecraftMessageUtil.sendMinecraftMessage(ReplacePlaceholderUtil.replacePlaceholderWithString(
                                ConfigOperation.getInfoOnPingFail(),
                                FormatPlaceholder.SERVER_NAME,
                                ConfigOperation.getServerName(),
                                FormatPlaceholder.SESSION_NAME,
                                sessionName,
                                FormatPlaceholder.SESSION_ID,
                                String.valueOf(sessionId),
                                FormatPlaceholder.PING_INTERVAL,
                                String.valueOf(heartbeatInterval),
                                FormatPlaceholder.REMOTE_ADDRESS,
                                remoteAddress,
                                FormatPlaceholder.WAIT_TIME,
                                String.valueOf(heartbeatCount)
                        ));

                        socket.close();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    isConnected = false;
                    logWarning(threadName, "出现异常，开始关闭Socket");

                    try {
                        socket.close();
                        logInfo(threadName, "Socket关闭成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        logWarning(threadName, "Socket关闭失败");
                    }
                }
            }

            allThreadCdl.countDown();
            noTakeQueueThreadCdl.countDown();
            logInfo(threadName, "结束工作");
        });

//        等待非取队列线程结束
        try {
            noTakeQueueThreadCdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        包id为-1的包不作任何处理
//        此处只是为了让队列不继续阻塞
        sendQueue.add(new Packet(new VarInt(0), new VarInt(-1), null));
        receiveQueue.add(new Packet(new VarInt(0), new VarInt(-1), null));

//        等待所有线程结束
        try {
            allThreadCdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logInfo("总线程", "所有线程均已停止工作");

        while (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        logInfo("Socket", "已确认关闭");
        isStop = true;
        mainThreadCdl.countDown();
    }

    public MinecraftConnectionThread(Server server, Socket socket, Logger logger, String sessionName, int heartbeatInterval, String remoteAddress, CountDownLatch mainThreadCdl) throws IOException {
        this.server = server;
        this.socket = socket;
        this.logger = logger;
        this.sessionName = sessionName;
        this.heartbeatInterval = heartbeatInterval;
        this.remoteAddress = remoteAddress;
        this.mainThreadCdl = mainThreadCdl;

        this.inputStream = new BufferedInputStream(socket.getInputStream());
        this.outputStream = new BufferedOutputStream(socket.getOutputStream());
    }
}
