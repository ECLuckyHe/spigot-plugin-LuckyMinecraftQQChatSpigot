package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.*;
import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.util.UserCommandUtil;
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
        dataPath.mkdirs();

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



    public static void addRconCommandUserCommand(String name, String command, String mapCommand) throws UserCommandExistException, IOException, UserCommandConflictException {
//        添加用户指令
//        用户指令可能发生冲突，以下是一个例子：
//        用户指令1：abc  #{aaa}    def
//        用户指令2：abc    bbb  #{bbb}
//        若用户执行/abc bbb def，则以上两条指令都符合该条件
//
//        因此需要在添加指令前确认新指令是否会与原指令冲突：
//        情况一：
//        用户指令1：abc  #{aaa}    def  #{bbb}
//        用户指令2：abc    bbb  #{aaa}
//        结论一：长度不同时，指令不冲突
//
//        情况二：
//        用户指令1：abc  #{aaa}  def  #{bbb}
//        用户指令2：abc     aaa  deg   #{ac}
//        结论二：长度相同时，对应位置上下都为常量，且有至少一对常量不相同，则不冲突，如例子中的def和deg
//
//        情况三：
//        用户指令1：abc  #{aaa}  def  #{bbb}
//        用户指令2：abc     aaa  def   #{ac}
//        结论三：长度相同时，对应位置上下都为常量，且每对常量都相同，则必定冲突
//               因为除去上下都为常量的列，其它列都至少包含一个参数，至多包含两个参数
//               同一列中如果一个为参数，一个为常量，则当参数等于另外一条指令对应位置的常量时匹配成功，因此指令冲突
//               同一列中如果两个都为参数，则填任意值都能匹配成功，因此指令冲突
//
//        最终结论：长度相同且所有对应位置上下都为相同常量值的两条用户指令必定冲突
//
//        * 未考虑如下不冲突情况：
//        用户指令1：abc  #{aaa}     def  #{aaa}
//        用户指令2：abc     bcd  #{aaa}     acg

        if (isRconCommandUserCommandNameExist(name)) {
            throw new UserCommandExistException();
        }

        List<Map<String, String>> userCommands = getRconCommandUserCommands();
        List<String> splitNewCommand = UserCommandUtil.splitCommand(command);
        List<String> splitNewCommandArgs = UserCommandUtil.getCommandArgList(command);

        for (Map<String, String> map : userCommands) {
            String existCommand = map.get("command");
            List<String> splitExistCommand = UserCommandUtil.splitCommand(existCommand);
            List<String> splitExistCommandArgs = UserCommandUtil.getCommandArgList(existCommand);

            if (splitExistCommand.size() != splitNewCommand.size()) {
//                长度不同不冲突
                continue;
            }

//            标记每个常量列都相等的情况，默认为true，若循环中修改为了false，则退出循环
            boolean isConstantEqual = true;
            for (int i = 0; i < splitExistCommand.size() && isConstantEqual; i++) {
//                长度相同则逐个比较
                String existPart = splitExistCommand.get(i);
                String newPart = splitNewCommand.get(i);

                if (!(splitExistCommandArgs.contains(existPart)) && (!splitNewCommandArgs.contains(newPart))) {
//                    都是常量
                    if (!existPart.equals(newPart)) {
                        isConstantEqual = false;
                        break;
                    }
                }
            }

            if (isConstantEqual) {
                throw new UserCommandConflictException(map.get("name"), map.get("command"), map.get("mapping"));
            }
        }

        Map<String, String> newMap = new HashMap<>();
        newMap.put("name", name);
        newMap.put("command", command);
        newMap.put("mapping", mapCommand);
        userCommands.add(newMap);

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


