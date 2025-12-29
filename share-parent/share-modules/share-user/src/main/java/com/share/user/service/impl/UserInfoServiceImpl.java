package com.share.user.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.core.context.SecurityContextHolder;
import com.share.common.core.exception.ServiceException;
import com.share.user.domain.UpdateUserLogin;
import com.share.user.domain.UserCountVo;
import com.share.user.domain.UserInfo;
import com.share.user.domain.UserLoginLog;
import com.share.user.mapper.UserInfoMapper;
import com.share.user.mapper.UserLoginLogMapper;
import com.share.user.service.IUserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户Service业务层处理
 *
 * @author atguigu
 * @date 2025-12-19
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService
{
    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserLoginLogMapper userLoginLogMapper;

    /**
     * 查询用户列表
     *
     * @param userInfo 用户
     * @return 用户
     */
    @Override
    public List<UserInfo> selectUserInfoList(UserInfo userInfo)
    {
        return userInfoMapper.selectUserInfoList(userInfo);
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public UserInfo wxLogin(String code) {
        System.out.println(1111);
        String openId = null;
        try {
            //获取openId
            WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            openId = sessionInfo.getOpenid();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("微信登录失败");
        }

        UserInfo userInfo = this.getOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getWxOpenId, openId));
        if (null == userInfo) {
            userInfo = new UserInfo();
            userInfo.setNickname(String.valueOf(System.currentTimeMillis()));
            userInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
            userInfo.setWxOpenId(openId);
            this.save(userInfo);
        }
        return userInfo;
    }

    @Override
    public Boolean updateUserLogin(UpdateUserLogin updateUserLogin) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(updateUserLogin.getUserId());
        userInfo.setLastLoginIp(updateUserLogin.getLastLoginIp());
        userInfo.setLastLoginTime(updateUserLogin.getLastLoginTime());
        userInfoMapper.updateById(userInfo);

        //登录日志
        UserLoginLog userLoginLog = new UserLoginLog();
        userLoginLog.setUserId(userInfo.getId());
        userLoginLog.setMsg("小程序登录");
        userLoginLog.setIpaddr(updateUserLogin.getLastLoginIp());
        userLoginLogMapper.insert(userLoginLog);
        return true;
    }

    @Override
    public Boolean isFreeDeposit() {
        //微信支付分
        //https://pay.weixin.qq.com/wiki/doc/apiv3/payscore.php?chapter=18_1&index=2
        // 默认免押金，模拟实现
        UserInfo userInfo = this.getById(SecurityContextHolder.getUserId());
        userInfo.setDepositStatus("1");
        this.updateById(userInfo);
        return true;
    }

    @Override
    public Map<String, Object> getUserCount() {
        List<UserCountVo> list = baseMapper.selectUserCount();

        Map<String, Object> map = new HashMap<>();
        //日期列表
        List<String> dateList = list.stream().map(UserCountVo::getRegisterDate).collect(Collectors.toList());
        //统计列表
        List<Integer> countList = list.stream().map(UserCountVo::getCount).collect(Collectors.toList());
        map.put("dateList", dateList);
        map.put("countList", countList);
        return map;
    }


}
