package com.share.device.emqx;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;

import java.util.Map;

public class JsonConvertUtil {

    /**
     * 将字符串转换为 JSONObject
     * 支持多种格式：
     * 1. 标准JSON格式：{"key1":"value1","key2":"value2"}
     * 2. 自定义格式：key1=value1|key2=value2
     * 3. URL查询参数格式：key1=value1&key2=value2
     * 4. 逗号分隔格式：key1=value1,key2=value2
     *
     * @param message 输入的字符串
     * @return 解析后的JSONObject，如果解析失败返回空JSONObject
     */
    public static JSONObject convertJson(String message) {
        // 创建Fastjson2的JSONObject
        JSONObject jsonObject = new JSONObject();

        if (message == null || message.trim().isEmpty()) {
            return jsonObject;
        }

        // 清理字符串：去除首尾空白和换行符
        String cleanedMessage = message.trim().replaceAll("[\r\n]", "");

        try {
            // 方案1：尝试作为标准JSON解析（Fastjson2的解析方式）
            if (isValidJsonFormat(cleanedMessage)) {
                return JSON.parseObject(cleanedMessage);
            }

            // 方案2：检查是否有常见分隔符
            String separator = determineSeparator(cleanedMessage);

            // 如果没有找到分隔符，直接返回空对象
            if (separator.isEmpty()) {
                return jsonObject;
            }

            // 根据分隔符解析
            return parseWithSeparator(cleanedMessage, separator);

        } catch (JSONException e) {
            // Fastjson2的JSON解析异常
            System.err.println("Fastjson2 JSON解析失败，输入: " + message);
            e.printStackTrace();
            return jsonObject;
        } catch (Exception e) {
            // 其他异常
            System.err.println("JSON转换失败，输入: " + message);
            e.printStackTrace();
            return jsonObject;
        }
    }

    /**
     * 判断字符串是否为有效的JSON格式
     */
    private static boolean isValidJsonFormat(String message) {
        String trimmed = message.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    /**
     * 检测字符串中的分隔符
     * 优先级：| > & > ,
     */
    private static String determineSeparator(String message) {
        // 统计各种分隔符出现的次数
        int pipeCount = countOccurrences(message, '|');
        int ampCount = countOccurrences(message, '&');
        int commaCount = countOccurrences(message, ',');
        int semicolonCount = countOccurrences(message, ';');

        // 检查是否有 = 号，确保是键值对格式
        if (message.contains("=")) {
            // 返回出现次数最多的分隔符
            if (pipeCount > 0 && pipeCount >= ampCount && pipeCount >= commaCount && pipeCount >= semicolonCount) {
                return "\\|";
            } else if (ampCount > 0 && ampCount >= commaCount && ampCount >= semicolonCount) {
                return "&";
            } else if (semicolonCount > 0 && semicolonCount >= commaCount) {
                return ";";
            } else if (commaCount > 0) {
                return ",";
            }
        }

        return "";
    }

    /**
     * 统计字符在字符串中出现的次数
     */
    private static int countOccurrences(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }

    /**
     * 使用指定分隔符解析字符串
     */
    private static JSONObject parseWithSeparator(String message, String separator) {
        JSONObject jsonObject = new JSONObject();

        // 使用正则表达式分割，避免空值
        String[] pairs = message.split(separator);

        for (String pair : pairs) {
            if (pair.trim().isEmpty()) {
                continue; // 跳过空字符串
            }

            // 使用 limit=2，确保值中可以包含 = 号
            String[] keyValue = pair.split("=", 2);

            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                // 移除值中可能存在的引号
                value = unquoteValue(value);

                // 尝试将字符串值转换为合适的类型
                Object convertedValue = tryConvertValue(value);

                // 使用Fastjson2的put方法
                jsonObject.put(key, convertedValue);
            } else if (keyValue.length == 1 && !pair.contains("=")) {
                // 对于没有值的键，设置为null
                jsonObject.put(pair.trim(), null);
            }
        }

        return jsonObject;
    }

    /**
     * 尝试将字符串值转换为合适的类型
     * Fastjson2会自动处理类型转换，但我们也可以提前转换以提高性能
     */
    private static Object tryConvertValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        String trimmedValue = value.trim();

        // 处理布尔值
        if ("true".equalsIgnoreCase(trimmedValue)) {
            return Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(trimmedValue)) {
            return Boolean.FALSE;
        }

        // 处理null值
        if ("null".equalsIgnoreCase(trimmedValue)) {
            return null;
        }

        // 尝试转换为整数
        try {
            if (!trimmedValue.contains(".")) {
                // 检查是否在整数范围内
                long longValue = Long.parseLong(trimmedValue);
                if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                    return (int) longValue;
                }
                return longValue;
            }
        } catch (NumberFormatException e) {
            // 不是整数，继续尝试
        }

        // 尝试转换为浮点数
        try {
            // 检查是否是科学计数法
            if (trimmedValue.matches("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?")) {
                double doubleValue = Double.parseDouble(trimmedValue);
                // 如果是整数的小数表示，转换为整数
                if (doubleValue == (int) doubleValue && !trimmedValue.contains(".")) {
                    return (int) doubleValue;
                }
                return doubleValue;
            }
        } catch (NumberFormatException e) {
            // 不是数字，返回原始字符串
        }

        return trimmedValue;
    }

    /**
     * 移除字符串值的引号
     */
    private static String unquoteValue(String value) {
        if (value == null || value.length() < 2) {
            return value;
        }

        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);

        // 移除成对的引号
        if ((first == '"' && last == '"') ||
                (first == '\'' && last == '\'') ||
                (first == '`' && last == '`')) {
            return value.substring(1, value.length() - 1);
        }

        return value;
    }

    /**
     * 扩展方法：将Map转换为JSONObject
     */
    public static JSONObject convertMapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return new JSONObject();
        }
        return new JSONObject(map);
    }

    /**
     * 扩展方法：将JSON字符串转换为指定类型的对象
     */
    public static <T> T convertJsonToObject(String message, Class<T> clazz) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }

        try {
            String cleanedMessage = message.trim().replaceAll("[\r\n]", "");

            // 如果是标准JSON格式，直接解析
            if (isValidJsonFormat(cleanedMessage)) {
                return JSON.parseObject(cleanedMessage, clazz);
            }

            // 否则先转换为JSONObject，再转换为目标对象
            JSONObject jsonObject = convertJson(cleanedMessage);
            return jsonObject.to(clazz);

        } catch (Exception e) {
            System.err.println("转换为对象失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 测试用例
        String[] testCases = {
                // 标准JSON
                "{\"name\":\"张三\",\"age\":25,\"city\":\"北京\",\"active\":true}",
                // 自定义格式
                "name=张三|age=25|city=北京|active=true|score=98.5",
                // URL参数格式
                "name=张三&age=25&city=北京&active=false&salary=15000.50",
                // 逗号分隔格式
                "name=张三,age=25,city=北京,married=false",
                // 分号分隔格式
                "name=李四;age=30;city=上海;balance=10000.00",
                // 包含特殊字符的值
                "key1=value=with=equals|key2=value&with&ampersand|key3=value,with,commas",
                // 带引号的值
                "name=\"张三\"|age=25|city='北京'",
                // 科学计数法
                "amount=1.23e5|rate=3.14E-2",
                // 大整数
                "id=123456789012345|timestamp=1640995200000",
                // 空值测试
                "",
                "   ",
                null,
                // 无效格式
                "not a valid format",
                // 只有键没有值
                "key1=|key2=value|key3"
        };

        for (int i = 0; i < testCases.length; i++) {
            String testCase = testCases[i];
            System.out.println("测试用例 " + (i + 1) + ":");
            System.out.println("输入: " + testCase);

            JSONObject result = convertJson(testCase);

            System.out.println("输出: " + result.toJSONString());
            System.out.println("长度: " + result.size());
            System.out.println("类型: " + result.getClass().getName());

            // 显示键值对的具体类型
            for (Map.Entry<String, Object> entry : result.entrySet()) {
                Object value = entry.getValue();
                System.out.println("  " + entry.getKey() + ": " + value +
                        " (类型: " + (value != null ? value.getClass().getSimpleName() : "null") + ")");
            }
            System.out.println("---\n");
        }

        // 测试转换为对象
        System.out.println("=== 测试转换为对象 ===");
        String jsonStr = "{\"name\":\"张三\",\"age\":25,\"city\":\"北京\"}";

        // 使用JSON.parseObject直接解析
        JSONObject obj1 = JSON.parseObject(jsonStr);
        System.out.println("直接解析: " + obj1);

        // 使用convertJson方法
        JSONObject obj2 = convertJson(jsonStr);
        System.out.println("convertJson: " + obj2);

        // 测试自定义格式
        String customStr = "name=李四|age=30|city=上海";
        JSONObject obj3 = convertJson(customStr);
        System.out.println("自定义格式: " + obj3);
    }
}
