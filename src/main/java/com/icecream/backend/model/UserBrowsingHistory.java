package com.icecream.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户浏览历史实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserBrowsingHistory extends BaseEntity {
    private Long userId;
    private Long postId;
}
