package com.share.user.factory;

import com.share.common.core.domain.R;
import com.share.user.api.RemoteUserInfoService;
import com.share.user.domain.UpdateUserLogin;
import com.share.user.domain.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 用户服务降级处理
 *
 * @author share
 */
@Component
public class RemoteUserInfoFallbackFactory implements FallbackFactory<RemoteUserInfoService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteUserInfoFallbackFactory.class);

    @Override
    public RemoteUserInfoService create(Throwable throwable)
    {
        log.error("用户服务调用失败:{}", throwable.getMessage());
        return new RemoteUserInfoService()
        {
            @Override
            public R<UserInfo> wxLogin(String code) {
                return R.fail("微信登录失败:" + throwable.getMessage());
            }

            @Override
            public R<Boolean> updateUserLogin(UpdateUserLogin updateUserLogin) {
                return R.fail("更新用户登录失败:" + throwable.getMessage());
            }

        };
    }
}
