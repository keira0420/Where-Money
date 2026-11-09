package shuhuai.wheremoney.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import shuhuai.wheremoney.service.excep.common.ServerException;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * JSON操作工具类
 * 提供JSON文件读取和解析功能
 */
public class JsonOperator {
    /**
     * 读取文件内容
     *
     * @param path 文件路径
     * @return 文件内容字符串
     * @throws IOException 读取文件时可能抛出的异常
     */
    public static String readFile(String path) throws IOException {
        File file = new File(path);
        FileReader fileReader = new FileReader(file);
        Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        int ch;
        StringBuilder stringBuilder = new StringBuilder();
        while ((ch = reader.read()) != -1) {
            stringBuilder.append((char) ch);
        }
        fileReader.close();
        reader.close();
        return stringBuilder.toString();
    }

    /**
     * 从JSON文件中获取指定名称的数组
     *
     * @param name JSON文件名（不含扩展名）和要获取的数组名称
     * @return JSON数组
     */
    public static JSONArray getMapFromJson(String name) {
        String json = null;
        try {
            json = readFile(System.getProperty("user.dir") + "\\src\\main\\resources\\json\\" + name + ".json");
        } catch (IOException error) {
            throw new ServerException("读取JSON文件失败");
        }
        JSONObject jsonObject = JSON.parseObject(json);
        if (jsonObject != null) {
            Object objArray = jsonObject.get(name);
            return JSON.parseArray(objArray.toString());
        } else {
            return null;
        }
    }
}