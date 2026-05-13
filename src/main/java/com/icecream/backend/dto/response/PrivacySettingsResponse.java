package com.icecream.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 隐私设置响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "隐私设置响应")
public class PrivacySettingsResponse {

    @Schema(description = "关注列表可见性：0-仅自己可见，1-所有人可见")
    private Integer followingVisibility;

    @Schema(description = "粉丝列表可见性：0-仅自己可见，1-所有人可见")
    private Integer followerVisibility;
}
