package com.share.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.share.device.domain.Region;

import java.util.List;

/**
 * @author: action
 * @create: 2025/12/16 15:29
 **/
public interface IRegionService extends IService<Region> {

    List<Region> treeSelect(String parentCode);

    String getNameByCode(String code);

}