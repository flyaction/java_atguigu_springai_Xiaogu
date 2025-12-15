package com.share.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.share.device.domain.PowerBank;

import java.util.List;

/**
 * @author: action
 * @create: 2025/12/15 17:49
 **/
public interface IPowerBankService extends IService<PowerBank>
{

    List<PowerBank> selectPowerBankList(PowerBank powerBank);

    PowerBank getByPowerBankNo(String powerBankNo);

    int savePowerBank(PowerBank powerBank);

    int updatePowerBank(PowerBank powerBank);
}