package com.leyou.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author: HuYi.Zhang
 * @create: 2018-04-24 17:20
 **/
public class JsonUtils {

    public static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    // 序列化   对象变成json字符串
    public static String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj.getClass() == String.class) {
            return (String) obj;
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("json序列化出错：" + obj, e);
            return null;
        }
    }

    // 反序列化   json字符串转对象
    public static <T> T toBean(String json, Class<T> tClass) {
        try {
            return mapper.readValue(json, tClass);
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    public static <E> List<E> toList(String json, Class<E> eClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, eClass));
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    public static <K, V> Map<K, V> toMap(String json, Class<K> kClass, Class<V> vClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructMapType(Map.class, kClass, vClass));
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }

    // 这种情况能解决一切问题，包括toList toMap什么的，只要类型正确就行
    public static <T> T nativeRead(String json, TypeReference<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            logger.error("json解析出错：" + json, e);
            return null;
        }
    }
/*
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class User{
        String name;
        Integer age;
    }

    public static void main(String[] args) {
        User user = new User("Jack",21);

        // 序列化
//        String json = toString(user);
//        System.out.println(json);

        // 反序列化
//        User user1 = toBean(json,User.class);
//        System.out.println("user1:" + user1);      // 没报错就ok

        // toList
        String json2 = "[20,-10,5,15]";
        List<Integer> list = toList(json2, Integer.class);
        System.out.println(list);

        // toMap
        String json3 = "{\"name\":\"Jack\", \"age\":\"21\"}";    // json里对象是map。 可以在引号里面alt+enter选择打开json编辑器，自动转格式
        Map<String, String> map = toMap(json3, String.class, String.class);
        System.out.println("map="+map);

        // nativeRead
        String json4 =  "[{\"name\":\"Jack\", \"age\":\"21\"},{\"name\":\"Rose\", \"age\":\"31\"}]";   // list里面是map，toList不能用了，map.class不行，里面的键值类型看不出
        List<Map<String, String>> maps = nativeRead(
                json4,
                new TypeReference< List<Map<String, String>> >() { }   // 匿名内部类；Map<String, String>.class不能这么写，但是可以用new对象的形式告诉它类型，TypeReference类型引用对象
        );
        for (Map<String, String> m : maps) {
            System.out.println("map = " + map);
        }

    }
    */
}
