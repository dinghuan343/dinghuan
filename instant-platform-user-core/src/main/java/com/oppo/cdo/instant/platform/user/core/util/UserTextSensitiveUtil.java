package com.oppo.cdo.instant.platform.user.core.util;

import com.oppo.cdo.instant.platform.common.sensitive.MD5Utils;
import org.apache.commons.lang3.StringUtils;

public class UserTextSensitiveUtil {


    private final static String USERNICKNAMEPREX = "user:nickname:%s:t:%s:uid:%s";

    private final  static String USERSIGNPREX = "user:usersign:%s:t:%s:uid:%s";

    public static  String getNickNameBizId(String nickName,String uid){
        String md5NickName = MD5Utils.encode(nickName);
        return String.format(USERNICKNAMEPREX, md5NickName,System.currentTimeMillis(),uid);
    }

    public static String getUserSignBizId(String userSign,String uid) {
        String md5UserSign = MD5Utils.encode(userSign);
        return  String.format(USERSIGNPREX,md5UserSign,System.currentTimeMillis(),uid);
    }

    public static  Boolean isNickeNameText(String bizId,String nickName) {
        if (StringUtils.isBlank(bizId)) {
            return false;
        }
        if (!bizId.contains("user:nickname")) {
            return false;
        }
        String textMd5 =   StringUtils.substringBetween(bizId,"nickname:",":t:");
        return compareMd5(textMd5,nickName);
    }

    public static  Boolean isUserSignText(String bizId,String userSign) {
        if (StringUtils.isBlank(bizId)) {
            return false;
        }
        if (!bizId.contains("user:usersign")) {
            return false;
        }
        String textMd5 =   StringUtils.substringBetween(bizId,"usersign:",":t:");
        return compareMd5(textMd5,userSign);
    }


    public static  String getUidByBizId(String bizId){
        if (StringUtils.isBlank(bizId)) {
            return null;
        }
        return StringUtils.substringAfterLast(bizId,"uid:");
    }

    public static Boolean isNeedInspectUser(String bizId) {
        if (StringUtils.isBlank(bizId)) {
            return false;
        }
        if (bizId.contains("user:")) {
            return true;
        }
        return false;
    }

    public static  Boolean compareMd5(String md5Text ,String textdata) {
        String md5Str2 = MD5Utils.encode(textdata);
        return md5Text.equals(md5Str2);
    }


    public static void main(String[] args) {
        System.out.println(StringUtils.substringBetween("user:nickname:0186eb9714b3643f795024535cee0e51:t:11111:uid:%s","nickname:",":t:"));
        System.out.println(MD5Utils.encode("ideal 右下角Git branches 没刷新显示分支缺失？:t:user:nickName"));
    }
}
