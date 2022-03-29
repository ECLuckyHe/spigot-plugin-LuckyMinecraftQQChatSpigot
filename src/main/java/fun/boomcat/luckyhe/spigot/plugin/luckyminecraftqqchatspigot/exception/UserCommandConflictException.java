package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception;


public class UserCommandConflictException extends Exception{
    private final String name;
    private final String command;
    private final String mapping;

    public UserCommandConflictException(String name, String command, String mapping) {
        super();
        this.name = name;
        this.command = command;
        this.mapping = mapping;
    }

    public String getName() {
        return name;
    }

    public String getCommand() {
        return command;
    }

    public String getMapping() {
        return mapping;
    }
}
