package com.share.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.share.user.domain.UserCountVo;
import com.share.user.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户Mapper接口
 *
 * @author atguigu
 * @date 2025-12-19
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo>
{

    /**
     * 查询用户列表
     *
     * @param userInfo 用户
     * @return 用户集合
     */
    public List<UserInfo> selectUserInfoList(UserInfo userInfo);

    /**
     * 获取用户统计信息
     * @return
     */
    List<UserCountVo> selectUserCount();
}
