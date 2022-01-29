package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config.ConfigOperation;
import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;

import java.io.IOException;

public class RconUtil {
    public static String sendMcCommad(String command) throws IOException, AuthenticationException {
        Rcon rcon = new Rcon("localhost", ConfigOperation.getRconCommandPort(), ConfigOperation.getRconCommandPassword().getBytes());
        String res = rcon.command(command);
        rcon.disconnect();

        return res;
    }
}
