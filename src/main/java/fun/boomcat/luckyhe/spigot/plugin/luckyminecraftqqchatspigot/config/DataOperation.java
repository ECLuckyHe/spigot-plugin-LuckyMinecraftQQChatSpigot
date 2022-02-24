package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.OpIdExistException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.OpIdNotExistException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.UserCommandExistException;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.UserCommandNotExistException;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataOperation {
    private static File dataPath;
    private static String dataFilename = "data.yml";
    private static Yaml yaml = new Yaml();
    private static Map<String, Object> dataMap;

    public static void initDataPath(File path, InputStream dataStream) throws IOException {
        dataPath = path;

        File[] files = dataPath.listFiles();
        boolean hasData = false;
        for (File file : files) {
            if (file.getName().equals(dataFilename)) {
                hasData = true;
                break;
            }
        }

        if (!hasData) {
            copyDataPathFromResource(dataStream);
        }
    }

    private static void copyDataPathFromResource(InputStream dataStream) throws IOException {
        FileOutputStream fos = new FileOutputStream(dataPath.getPath() + "/" + dataFilename);
        int len;
        byte[] buf = new byte[1024];
        while ((len = dataStream.read(buf)) > 0) {
            fos.write(buf, 0, len);
        }
        dataStream.close();
        fos.close();
    }

    private static Map<String, Object> getDataMap() throws FileNotFoundException {
        if (dataMap == null) {
            dataMap = yaml.load(new InputStreamReader(new FileInputStream(dataPath.getPath() + "/" + dataFilename), StandardCharsets.UTF_8));
        }

        return dataMap;
    }

    private static void writeFile() throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(dataPath.getPath() + "/" + dataFilename), StandardCharsets.UTF_8);
        yaml.dump(getDataMap(), osw);
        osw.close();

        dataMap = null;
    }

    public static List<Object> getRconCommandOpIds() throws FileNotFoundException {
        Map<String, Object> dataMap = getDataMap();
        Map<String, Object> rconCommandMap = (Map<String, Object>) dataMap.get("rconCommand");

        return (List<Object>) rconCommandMap.get("opIds");
    }

    public static boolean isRconCommandOpIdExist(long id) throws FileNotFoundException {
        List<Object> rconCommandOpIds = getRconCommandOpIds();

        for (Object rconCommandOpId : rconCommandOpIds) {
            long opId;
            if (rconCommandOpId instanceof Integer) {
                opId = (int) rconCommandOpId;
            } else {
                opId = (long) rconCommandOpId;
            }

            if (opId == id) {
                return true;
            }
        }

        return false;
    }

    public static void addRconCommandOpIds(long id) throws IOException, OpIdExistException {
        if (isRconCommandOpIdExist(id)) {
            throw new OpIdExistException();
        }

        List<Object> rconCommandOpIds = getRconCommandOpIds();
        rconCommandOpIds.add(id);
        writeFile();
    }

    public static void removeRconCommandIds(long id) throws IOException, OpIdNotExistException {
        if (!isRconCommandOpIdExist(id)) {
            throw new OpIdNotExistException();
        }

        List<Object> rconCommandOpIds = getRconCommandOpIds();
        for (Object rconCommandOpId : rconCommandOpIds) {
            long opId;
            if (rconCommandOpId instanceof Integer) {
                opId = (int) rconCommandOpId;
            } else {
                opId = (long) rconCommandOpId;
            }

            if (opId == id) {
                rconCommandOpIds.remove(rconCommandOpId);
                break;
            }
        }
        writeFile();
    }

    public static List<Map<String, String>> getRconCommandUserCommands() throws FileNotFoundException {
        return ((List<Map<String, String>>) ((Map<String, Object>) getDataMap().get("rconCommand")).get("userCommands"));
    }

    public static boolean isRconCommandUserCommandNameExist(String name) throws FileNotFoundException {
        return getRconCommandUserCommandByName(name) != null;
    }

    public static Map<String, String> getRconCommandUserCommandByName(String name) throws FileNotFoundException {
        List<Map<String, String>> rconCommandUserCommands = getRconCommandUserCommands();
        for (Map<String, String> map : rconCommandUserCommands) {
            if (map.get("name").equals(name)) {
                return map;
            }
        }

        return null;
    }



    public static void addRconCommandUserCommand(String name, String command, String mapCommand) throws UserCommandExistException, IOException {
//        添加用户指令
        if (isRconCommandUserCommandNameExist(name)) {
            throw new UserCommandExistException();
        }

        List<Map<String, String>> rconCommandUserCommands = getRconCommandUserCommands();

        Map<String, String> newMap = new HashMap<>();
        newMap.put("name", name);
        newMap.put("command", command);
        newMap.put("mapping", mapCommand);
        rconCommandUserCommands.add(newMap);

        writeFile();
    }

    public static void delRconCommandUserCommand(String name) throws IOException, UserCommandNotExistException {
        if (!isRconCommandUserCommandNameExist(name)) {
            throw new UserCommandNotExistException();
        }

        List<Map<String, String>> rconCommandUserCommands = getRconCommandUserCommands();
        rconCommandUserCommands.removeIf(o -> o.get("name").equals(name));
        writeFile();
    }
}


