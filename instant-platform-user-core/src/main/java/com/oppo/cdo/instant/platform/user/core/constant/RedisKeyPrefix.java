package com.oppo.cdo.instant.platform.user.core.constant;

/**
 * description:
 * redis的key统一维护
 * key-prefix规则:  user:{{}}:   如 user:session:
 *
 * @author ouyangrenyong
 * @since
 */
public class RedisKeyPrefix {

    /**
     * 用户的session信息，一个hash结构
     */
    public static final String USER_SESSION = "user:session:";

    /**
     * hall play status:
     * PLAYER_STATUS_FREE-10050 PLAYER_STATUS_MATCHING-10051 PLAYER_STATUS_PREPAREING-10052 PLAYER_STATUS_FIGHTING-10053
     */
    public static final String KEY_HALL_USER_ACTIVITY_STATUS_HEAD = "test:hall:user:activity:status:";

    public static final String KEY_USER_PLAT_CODE_HEAD = "test:hall:user:plat:code:";

    public static final String KEY_USER_DEV_HEAD = "i_u_dev_";

    public static final String KEY_UID_CHANNELUID_HEAD = "i_uid_chu_";

    public static final String KEY_CHANNELUID_UID_HEAD = "i_chu_uid_";
}
