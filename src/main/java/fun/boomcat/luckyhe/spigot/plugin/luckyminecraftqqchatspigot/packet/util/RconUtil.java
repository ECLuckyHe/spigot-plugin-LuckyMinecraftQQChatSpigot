package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.ConfigOperation;
import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;

import java.io.IOException;

public class RconUtil {
    public static String sendMcCommad(String command) throws IOException, AuthenticationException {
        Rcon rcon = new Rcon("localhost", ConfigOperation.getRconCommandPort(), ConfigOperation.getRconCommandPassword().getBytes());
        String res = null;
        try {
            res = rcon.command(command);
        } catch (Exception e) {
            return "执行异常";
        } finally {
            rcon.disconnect();
        }

        if (res.equals("")) {
            res = "无返回结果";
        }

        return res;
    }
}
