package com.zj.client.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class CompareJsonUtils {
    public static Map<String, Object> compareJsonObject(String responseJson, String expectJson) {
        //将字符串转换为json对象
        JSON oldJson = JSON.parseObject(responseJson);
        JSON newJson = JSON.parseObject(expectJson);
        //递归遍历json对象所有的key-value，将其封装成path:value格式进行比较
        Map<String, Object> responseMap = new LinkedHashMap<>();
        Map<String, Object> expectMap = new LinkedHashMap<>();
        convertJsonToMap(oldJson, "", responseMap);
        convertJsonToMap(newJson, "", expectMap);
        return compareExpectWithResponse(responseMap, expectMap);
    }

    /**
     * 将json数据转换为map存储用于比较
     */
    private static void convertJsonToMap(Object json, String root, Map<String, Object> resultMap) {
        if (json instanceof JSONObject) {
            JSONObject jsonObject = ((JSONObject) json);
            Iterator iterator = jsonObject.keySet().iterator();
            while (iterator.hasNext()) {
                Object key = iterator.next();
                Object value = jsonObject.get(key);
                String newRoot = "".equals(root) ? key + "" : root + "." + key;
                if (value instanceof JSONObject || value instanceof JSONArray) {
                    convertJsonToMap(value, newRoot, resultMap);
                } else {
                    resultMap.put(newRoot, value);
                }
            }
        } else if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;
            for (int i = 0; i < jsonArray.size(); i++) {
                Object value = jsonArray.get(i);
                String newRoot = "".equals(root) ? "[" + i + "]" : root + ".[" + i + "]";
                if (value instanceof JSONObject || value instanceof JSONArray) {
                    convertJsonToMap(value, newRoot, resultMap);
                } else {
                    resultMap.put(newRoot, value);
                }
            }
        }
    }

    /**
     * 將新的map與舊的比較，並將數據統一存在舊的裡面
     * @param responseMap
     * @param expectMap
     */
    private static Map<String, Object> compareExpectWithResponse(Map<String, Object> responseMap, Map<String, Object> expectMap) {
        Map<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<String, Object> item : expectMap.entrySet()) {
            String key = item.getKey();
            Object newValue = item.getValue();
            Map<String, Object> differenceMap = new HashMap<>();
            if (responseMap.containsKey(key)) {
                Object oldValue = responseMap.get(key);
                if (!Objects.equals(oldValue, newValue)) {
                    differenceMap.put("oldValue", oldValue);
                    differenceMap.put("newValue", newValue);
                    resultMap.put(key, differenceMap);
                }
            } else {
                differenceMap.put("oldValue", "");
                differenceMap.put("newValue", newValue);
                resultMap.put(key, differenceMap);
            }
        }
        return resultMap;
    }

    /**
     * 将已经找出不同数据的map根据key的层级结构封装成json返回
     *
     * @param map
     * @return
     */
    private static String convertMapToJson(Map<String, Object> map) {
        JSONObject resultJSONObject = new JSONObject();
        for (Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> item = it.next();
            String key = item.getKey();
            Object value = item.getValue();
            String[] paths = key.split("\\.");
            int i = 0;
            Object remarkObject = null;//用於深度標識對象
            int indexAll = paths.length - 1;
            while (i <= paths.length - 1) {
                String path = paths[i];
                if (i == 0) {
                    //初始化对象标识
                    if (resultJSONObject.containsKey(path)) {
                        remarkObject = resultJSONObject.get(path);
                    } else {
                        if (indexAll > i) {
                            if (paths[i + 1].matches("\\[[0-9]+\\]")) {
                                remarkObject = new JSONArray();
                            } else {
                                remarkObject = new JSONObject();
                            }
                            resultJSONObject.put(path, remarkObject);
                        } else {
                            resultJSONObject.put(path, value);
                        }
                    }
                    i++;
                    continue;
                }
                if (path.matches("\\[[0-9]+\\]")) {//匹配集合对象
                    int startIndex = path.lastIndexOf("[");
                    int endIndext = path.lastIndexOf("]");
                    int index = Integer.parseInt(path.substring(startIndex + 1, endIndext));
                    if (indexAll > i) {
                        if (paths[i + 1].matches("\\[[0-9]+\\]")) {
                            while (((JSONArray) remarkObject).size() <= index) {
                                if(((JSONArray) remarkObject).size() == index){
                                    ((JSONArray) remarkObject).add(index,new JSONArray());
                                }else{
                                    ((JSONArray) remarkObject).add(null);
                                }
                            }
                        } else {
                            while(((JSONArray) remarkObject).size() <= index){
                                if(((JSONArray) remarkObject).size() == index){
                                    ((JSONArray) remarkObject).add(index,new JSONObject());
                                }else{
                                    ((JSONArray) remarkObject).add(null);
                                }
                            }
                        }
                        remarkObject = ((JSONArray) remarkObject).get(index);
                    } else {
                        while(((JSONArray) remarkObject).size() <= index){
                            if(((JSONArray) remarkObject).size() == index){
                                ((JSONArray) remarkObject).add(index, value);
                            }else{
                                ((JSONArray) remarkObject).add(null);
                            }
                        }
                    }
                } else {
                    if (indexAll > i) {
                        if (paths[i + 1].matches("\\[[0-9]+\\]")) {
                            if(!((JSONObject) remarkObject).containsKey(path)){
                                ((JSONObject) remarkObject).put(path, new JSONArray());
                            }
                        } else {
                            if(!((JSONObject) remarkObject).containsKey(path)){
                                ((JSONObject) remarkObject).put(path, new JSONObject());
                            }
                        }
                        remarkObject = ((JSONObject) remarkObject).get(path);
                    } else {
                        ((JSONObject) remarkObject).put(path, value);
                    }
                }
                i++;
            }
        }
        return JSON.toJSONString(resultJSONObject);
    }

    public static void main(String[] args){
        String oldStr= "{a:'aaa',b:'bbb',c:{'d':'ddd'}}";
        String newStr= "{a:'aa',b:'bb',c:{'d':'d'}}";
        Map<String, Object> stringObjectMap = new CompareJsonUtils().compareJsonObject(oldStr, newStr);
        System.out.println(stringObjectMap);

        System.out.println(convertMapToJson(stringObjectMap));;
    }
}
