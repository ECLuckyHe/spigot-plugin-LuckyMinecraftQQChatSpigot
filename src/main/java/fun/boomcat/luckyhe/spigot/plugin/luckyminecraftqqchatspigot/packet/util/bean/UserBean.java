package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.packet.util.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserBean {
    private String name;
    private String uuid;
}
