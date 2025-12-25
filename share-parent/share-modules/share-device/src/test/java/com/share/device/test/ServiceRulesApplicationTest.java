package com.share.device.test;

import com.alibaba.fastjson2.JSONObject;
import com.share.device.emqx.JsonConvertUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ServiceRulesApplicationTest {


    @Test
    void test2(){

        String str  = "{\"pNo\":\"cdb002\",\"uId\":3,\"cNo\":\"gj001\",\"sNo\":\"2\",\"mNo\":\"mm3c19z2a7\"}";

        //String str  = "{\"name\":\"张三\",\"age\":25,\"city\":\"北京\",\"active\":true}";

        System.out.println(str);

        //str = JSONObject.toJSONString( str);

        JSONObject jsonMessage = JsonConvertUtil.convertJson(str);


        System.out.println(jsonMessage.toJSONString());

        System.out.println(jsonMessage.getString("mNo"));

    }

}
