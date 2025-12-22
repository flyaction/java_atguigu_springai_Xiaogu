package com.share.device.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.share.common.core.exception.ServiceException;
import com.share.device.service.IMapService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: action
 * @create: 2025/12/22 15:51
 **/
public class MapServiceImpl implements IMapService {

    @Value("${tencent.map.key}")
    private String key;

    @Resource
    private RestTemplate restTemplate;

    @Override
    public Double calculateDistance(String startLongitude, String startLatitude, String endLongitude, String endLatitude) {
        String url = "https://apis.map.qq.com/ws/direction/v1/walking/?from={from}&to={to}&key={key}";

        Map<String, String> map = new HashMap<>();
        map.put("from", startLatitude + "," + startLongitude);
        map.put("to", endLatitude + "," + endLongitude);
        map.put("key", key);

        JSONObject result = restTemplate.getForObject(url, JSONObject.class, map);
        if(result.getIntValue("status") != 0) {
            throw new ServiceException("地图服务调用失败");
        }

        //返回第一条最佳线路
        JSONObject route = result.getJSONObject("result").getJSONArray("routes").getJSONObject(0);
        // 单位：米
        return route.getBigDecimal("distance").doubleValue();
    }
}
