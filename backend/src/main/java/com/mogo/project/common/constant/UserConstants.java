package com.mogo.project.common.constant;

/**
 * 用户/系统通用常量信息
 */
public class UserConstants {

    /**
     * 平台内系统用户的唯一标志
     */
    public static final String SYS_USER = "SYS_USER";

    /** 正常状态 */
    public static final Integer DEPT_NORMAL = 1;

    /** 停用状态 */
    public static final Integer DEPT_DISABLE = 0;

    /** 校验返回结果码 */
    public static final boolean UNIQUE = true;
    public static final boolean NOT_UNIQUE = false;

    /**
     * 校验返回结果码 (如果你的校验逻辑习惯用 String，保留下面这两个，否则用上面的 boolean)
     */
    public static final String UNIQUE_STR = "0";
    public static final String NOT_UNIQUE_STR = "1";

    /**
     * 用户名长度限制
     */
    public static final int USERNAME_MIN_LENGTH = 2;
    public static final int USERNAME_MAX_LENGTH = 20;

    /**
     * 密码长度限制
     */
    public static final int PASSWORD_MIN_LENGTH = 5;
    public static final int PASSWORD_MAX_LENGTH = 20;
}