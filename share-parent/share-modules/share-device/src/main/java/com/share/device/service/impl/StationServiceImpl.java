package com.share.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.device.domain.Cabinet;
import com.share.device.domain.Station;
import com.share.device.mapper.StationMapper;
import com.share.device.service.IStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StationServiceImpl extends ServiceImpl<StationMapper, Station> implements IStationService
{
    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private CabinetServiceImpl cabinetService;

    @Override
    public List<Station> selectStationList(Station station)
    {
        List<Station> list = stationMapper.selectStationList(station);
        List<Long> cabinetIdList = list.stream().map(Station::getCabinetId).collect(Collectors.toList());
        Map<Long,String> cabinetIdToCabinetNoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(cabinetIdList)) {
            List<Cabinet> cabinetList = cabinetService.list(new LambdaQueryWrapper<Cabinet>().in(Cabinet::getId, cabinetIdList));
            cabinetIdToCabinetNoMap = cabinetList.stream().collect(Collectors.toMap(Cabinet::getId, Cabinet::getCabinetNo));
        }
        for (Station item : list) {
            item.setCabinetNo(cabinetIdToCabinetNoMap.get(item.getCabinetId()));
        }
        return list;
    }
    
}