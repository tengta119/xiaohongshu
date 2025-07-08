package com.quanxiaoha.xiaohashu.user.biz.model.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lbwxxc
 * @date 2025/7/8 14:43
 * @description:
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FindUserProfileRspVO {

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 小哈书 ID
     */
    private String xiaohashuId;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 岁数
     */
    private Integer age;

    /**
     * 个人介绍
     */
    private String introduction;

    /**
     * 关注数
     */
    private String followingTotal;

    /**
     * 粉丝数
     */
    private String fansTotal;

    /**
     * 点赞与收藏总数
     */
    private String likeAndCollectTotal;

    /**
     * 当前发布笔记数
     */
    private String noteTotal;

    /**
     * 当前获得点赞数
     */
    private String likeTotal;

    /**
     * 当前获得的收藏数
     */
    private String collectTotal;
}
