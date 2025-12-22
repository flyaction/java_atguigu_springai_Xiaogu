package com.share.device.service;

/**
 * @author: action
 * @create: 2025/12/22 15:51
 **/
public interface IMapService {

    /**
     * 计算两个经纬度之间的距离
     * @param startLongitude
     * @param startLatitude
     * @param endLongitude
     * @param endLatitude
     * @return
     */
    Double calculateDistance(String startLongitude,String startLatitude,String endLongitude,String endLatitude);

}
