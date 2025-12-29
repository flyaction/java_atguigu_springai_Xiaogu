package com.share.user.api;

import com.share.common.core.constant.ServiceNameConstants;
import com.share.common.core.domain.R;
import com.share.user.domain.UpdateUserLogin;
import com.share.user.domain.UserInfo;
import com.share.user.factory.RemoteUserInfoFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 用户服务
 *
 * @author share
 */
@FeignClient(contextId = "remoteUserInfoService", value = ServiceNameConstants.SHARE_USER, fallbackFactory = RemoteUserInfoFallbackFactory.class)
public interface RemoteUserInfoService
{
    @GetMapping("/userInfo/wxLogin/{code}")
    public R<UserInfo> wxLogin(@PathVariable("code") String code);

    @PutMapping("/userInfo/updateUserLogin")
    public R<Boolean> updateUserLogin(@RequestBody UpdateUserLogin updateUserLogin);

    @GetMapping(value = "/userInfo/getUserInfo/{id}")
    public R<UserInfo> getInfo(@PathVariable("id") Long id);

    @GetMapping(value = "/userInfo/getUserCount")
    public R<Map<String, Object>> getUserCount();


}
