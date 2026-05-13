package com.icecream.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 隐私设置请求DTO
 */
@Data
@Schema(description = "隐私设置请求")
public class PrivacySettingsRequest {

    @Min(value = 0, message = "关注列表可见性取值只能为0或1")
    @Max(value = 1, message = "关注列表可见性取值只能为0或1")
    @Schema(description = "关注列表可见性：0-仅自己可见，1-所有人可见", example = "1")
    private Integer followingVisibility;

    @Min(value = 0, message = "粉丝列表可见性取值只能为0或1")
    @Max(value = 1, message = "粉丝列表可见性取值只能为0或1")
    @Schema(description = "粉丝列表可见性：0-仅自己可见，1-所有人可见", example = "1")
    private Integer followerVisibility;
}
