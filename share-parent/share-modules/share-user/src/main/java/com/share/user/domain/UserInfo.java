package com.share.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.share.common.core.annotation.Excel;

/**
 * 用户对象 user_info
 *
 * @author atguigu
 * @date 2025-12-19
 */
@Data
@Schema(description = "用户")
public class UserInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 微信openId */
    @Excel(name = "微信openId")
    @Schema(description = "微信openId")
    private String wxOpenId;

    /** 会员昵称 */
    @Excel(name = "会员昵称")
    @Schema(description = "会员昵称")
    private String nickname;

    /** 性别 */
    @Excel(name = "性别")
    @Schema(description = "性别")
    private String gender;

    /** 头像 */
    @Excel(name = "头像")
    @Schema(description = "头像")
    private String avatarUrl;

    /** 电话 */
    @Excel(name = "电话")
    @Schema(description = "电话")
    private String phone;

    /** 1有效，2禁用 */
    @Excel(name = "1有效，2禁用")
    @Schema(description = "1有效，2禁用")
    private String status;

}
