package com.oppo.cdo.instant.platform.user.core.util;

import com.oppo.cdo.instant.platform.common.base.util.PlatformIdUtil;
import com.oppo.cdo.instant.platform.user.core.convertor.UserInfoConvertor;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UserBasicDTO;
import com.oppo.cdo.instant.platform.user.entity.UserBaseInfoEntity;

/**
 * description:
 *
 * @author ouyangrenyong
 * @since 2.0
 */
public class UserBeanConvertor {

    public static UserBasicDTO convert2UserBasicDTO(UserBaseInfoEntity userBasicInfoDto) {
        if (null == userBasicInfoDto) {
            return null;
        }
        UserBasicDTO basicInfoDto = new UserBasicDTO();
        basicInfoDto.setAge(userBasicInfoDto.getAge());
        basicInfoDto.setAvatar(userBasicInfoDto.getAvatar());
        basicInfoDto.setSex(userBasicInfoDto.getSex());
        basicInfoDto.setUid(userBasicInfoDto.getUid());
        basicInfoDto.setOid(String.valueOf(PlatformIdUtil.makeOId(userBasicInfoDto.getUid())));
        basicInfoDto.setAid(userBasicInfoDto.getAid());
        basicInfoDto.setNickName(userBasicInfoDto.getNickName());
        basicInfoDto.setLoginType(userBasicInfoDto.getLoginType());
        basicInfoDto.setLocation(userBasicInfoDto.getLocation());
        basicInfoDto.setBirthday(userBasicInfoDto.getBirthday());
        basicInfoDto.setAddress(userBasicInfoDto.getAddress());
        basicInfoDto.setEmail(userBasicInfoDto.getEmail());
        basicInfoDto.setConstellation(UserInfoConvertor.convertEntityGetConstellation(userBasicInfoDto.getBirthday(),userBasicInfoDto.getLocation()));
        basicInfoDto.setRobot(AIUtil.isAI(userBasicInfoDto.getUid()));
        basicInfoDto.setUserSign(userBasicInfoDto.getUserSign());
        return basicInfoDto;
    }
}
