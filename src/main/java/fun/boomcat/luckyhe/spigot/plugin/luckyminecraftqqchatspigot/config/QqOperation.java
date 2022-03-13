package fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.config;

import fun.boomcat.luckyhe.spigot.plugin.luckyminecraftqqchatspigot.exception.UserBindNotExistException;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QqOperation {
    private static File dataPath;
    private static String dataFilename = "qq.yml";
    private static Yaml yaml = new Yaml();
    private static List<Map<String, Object>> dataList;

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

    private static List<Map<String, Object>> getDataList() throws FileNotFoundException {
        if (dataList == null) {
            dataList = yaml.load(new InputStreamReader(new FileInputStream(dataPath.getPath() + "/" + dataFilename), StandardCharsets.UTF_8));
        }

        return dataList;
    }

    private static void writeFile() throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(dataPath.getPath() + "/" + dataFilename), StandardCharsets.UTF_8);
        yaml.dump(getDataList(), osw);
        osw.close();

        dataList = null;
    }

    public static String getMcIdByQq(long qq) throws FileNotFoundException {
        List<Map<String, Object>> dataList = getDataList();
        for (Map<String, Object> map : dataList) {
            Object o = map.get("qq");
            long storedQq = o instanceof Integer ? (int) o : (long) o;
            if (qq == storedQq) {
                return (String) map.get("id");
            }
        }
        return null;
    }

    public static List<Long> getQqsByMcid(String mcid) throws FileNotFoundException {
//        获取绑定了该mcid的qq列表
        List<Map<String, Object>> dataList = getDataList();
        List<Long> res = new ArrayList<>();
        for (Map<String, Object> map : dataList) {
            if (map.get("id").equals(mcid)) {
                Object qqObject = map.get("qq");
                long qq = qqObject instanceof Integer ? (int) qqObject : (long) qqObject;
                res.add(qq);
            }
        }

        return res;
    }

    public static void unbind(long qq, String id) throws IOException, UserBindNotExistException {
        List<Map<String, Object>> dataList = getDataList();

        Map<String, Object> existMap = null;
        for (Map<String, Object> map : dataList) {
            Object o = map.get("qq");
            long existQq = o instanceof Integer ? (int) o : (long) o;
            if (existQq == qq && map.get("id").equals(id)) {
                existMap = map;
            }
        }

        if (existMap == null) {
            throw new UserBindNotExistException();
        }

        getDataList().remove(existMap);

        writeFile();
    }

    public static void bind(long qq, String id) throws IOException {
        List<Map<String, Object>> dataList = getDataList();

        Map<String, Object> existMap = null;
        for (Map<String, Object> map : dataList) {
            Object o = map.get("qq");
            long storedQq = o instanceof Integer ? (int) o : (long) o;
            if (storedQq == qq) {
                existMap = map;
                break;
            }
        }

        if (existMap == null) {
            Map<String, Object> newMap = new HashMap<>();
            newMap.put("qq", qq);
            newMap.put("id", id);
            dataList.add(newMap);
        } else {
            existMap.put("id", id);
        }

        writeFile();
    }
}
