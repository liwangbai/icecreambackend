package com.icecream.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码请求DTO
 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordRequest {

    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "旧密码", example = "oldPassword123")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 50, message = "新密码长度6-50个字符")
    @Schema(description = "新密码，长度6-50个字符", example = "newPassword456")
    private String newPassword;
}
