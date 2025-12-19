package com.share.user.mapper;

import java.util.List;
import com.share.user.domain.UserLoginLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 用户登录记录Mapper接口
 *
 * @author atguigu
 * @date 2025-12-19
 */
public interface UserLoginLogMapper extends BaseMapper<UserLoginLog>
{

    /**
     * 查询用户登录记录列表
     *
     * @param userLoginLog 用户登录记录
     * @return 用户登录记录集合
     */
    public List<UserLoginLog> selectUserLoginLogList(UserLoginLog userLoginLog);

}
