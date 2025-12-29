package com.share.ai.controller;

import com.share.common.core.domain.R;
import com.share.common.core.web.controller.BaseController;
import com.share.common.core.web.domain.AjaxResult;
import com.share.user.api.RemoteUserInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "AI数据统计")
@RestController
@RequestMapping("/sta")
public class UserStasticsController extends BaseController {

    @Resource
    private RemoteUserInfoService remoteUserService;

    @GetMapping("/userCount")
    public AjaxResult userCount() {
        R<Map<String,Object>> result = remoteUserService.getUserCount();
        Map<String, Object> map = result.getData();
        return success(map);
    }
}
