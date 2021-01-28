package com.oppo.cdo.instant.platform.user.core.convertor;

import com.oppo.cdo.instant.platform.common.base.util.SpringBeanUtil;
import com.oppo.cdo.instant.platform.common.base.util.TimeZoneUtils;
import com.oppo.cdo.instant.platform.user.core.service.PlatTokenManagerService;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UserBasicDTO;
import com.oppo.cdo.instant.platform.user.entity.UserBaseInfoEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserInfoConvertor {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoConvertor.class);

    public static Integer convertEntityGetConstellation(String birthday, String location) {
        int constellation = 0;
        if (StringUtils.isNotEmpty(birthday)) {
            try {
                String timeStr = TimeZoneUtils.getSpecifyTimezoneDateStringByRegion(Long.valueOf(birthday), location);
                String date = timeStr.split(" ")[0];
                int month = Integer.valueOf(date.split("-")[1]);
                int day = Integer.valueOf(date.split("-")[2]);
                switch (month) {
                    case 1:
                        constellation = day < 21 ? 10 : 11;
                        break;
                    case 2:
                        constellation = day < 20 ? 11 : 12;
                        break;
                    case 3:
                        constellation = day < 21 ? 12 : 1;
                        break;
                    case 4:
                        constellation = day < 21 ? 1 : 2;
                        break;
                    case 5:
                        constellation = day < 22 ? 2 : 3;
                        break;
                    case 6:
                        constellation = day < 22 ? 3 : 4;
                        break;
                    case 7:
                        constellation = day < 23 ? 4 : 5;
                        break;
                    case 8:
                        constellation = day < 24 ? 5 : 6;
                        break;
                    case 9:
                        constellation = day < 24 ? 6 : 7;
                        break;
                    case 10:
                        constellation = day < 24 ? 7 : 8;
                        break;
                    case 11:
                        constellation = day < 23 ? 8 : 9;
                        break;
                    case 12:
                        constellation = day < 22 ? 9 : 10;
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                logger.error("convertEntityGetConstellation birthday {}, location {}", e);
            }
        }

        return constellation;
    }

    // 现在aid是完全相同的
    public static String convertEntityGetAid(String uid) {
        PlatTokenManagerService platTokenManagerService = SpringBeanUtil.getBeanByType(PlatTokenManagerService.class);
        return platTokenManagerService.getSecretUid(uid);
    }

//    public static UserBasicInfoDto convertEntityToDto(UserBaseInfoEntity userBasicInfoEntity) {
//        UserBasicInfoDto basicInfoDto = new UserBasicInfoDto();
//        basicInfoDto.setAge(userBasicInfoEntity.getAge());
//        basicInfoDto.setAvatar(userBasicInfoEntity.getAvatar());
//        basicInfoDto.setSex(userBasicInfoEntity.getSex());
//        basicInfoDto.setUid(userBasicInfoEntity.getUid());
//        basicInfoDto.setOid(userBasicInfoEntity.getOid());
//        basicInfoDto.setAid(convertEntityGetAid(userBasicInfoEntity.getUid()));
//        basicInfoDto.setOpenId(userBasicInfoEntity.getOpenId());
//        basicInfoDto.setNickName(userBasicInfoEntity.getNickName());
//        basicInfoDto.setLoginType(userBasicInfoEntity.getLoginType());
//        basicInfoDto.setUserSign(userBasicInfoEntity.getUserSign());
//
//        basicInfoDto.setLoginType(userBasicInfoEntity.getLoginType());
//        basicInfoDto.setLocation(userBasicInfoEntity.getLocation());
//        basicInfoDto.setBirthday(userBasicInfoEntity.getBirthday());
//        basicInfoDto.setAddress(userBasicInfoEntity.getAddress());
//        basicInfoDto.setEmail(userBasicInfoEntity.getEmail());
//        // 这点计算星座
//        basicInfoDto.setConstellation(convertEntityGetConstellation(userBasicInfoEntity.getBirthday(),
//                                                                    userBasicInfoEntity.getLocation()));
//        return basicInfoDto;
//    }

//    public static UserBasicDTO convertInfoToDetail(UserBasicInfoDto userBasicInfoDto) {
//        UserBasicDTO basicInfoDto = new UserBasicDTO();
//        basicInfoDto.setAge(userBasicInfoDto.getAge());
//        basicInfoDto.setAvatar(userBasicInfoDto.getAvatar());
//        basicInfoDto.setSex(userBasicInfoDto.getSex());
//        basicInfoDto.setUid(userBasicInfoDto.getUid());
//        basicInfoDto.setOid(userBasicInfoDto.getOid());
//        basicInfoDto.setAid(userBasicInfoDto.getAid());
//        basicInfoDto.setNickName(userBasicInfoDto.getNickName());
//        basicInfoDto.setLoginType(userBasicInfoDto.getLoginType());
//        basicInfoDto.setLocation(userBasicInfoDto.getLocation());
//        basicInfoDto.setBirthday(userBasicInfoDto.getBirthday());
//        basicInfoDto.setAddress(userBasicInfoDto.getAddress());
//        basicInfoDto.setEmail(userBasicInfoDto.getEmail());
//        basicInfoDto.setConstellation(userBasicInfoDto.getConstellation());
//        basicInfoDto.setRobot(AIUtil.isAI(userBasicInfoDto.getUid()));
//        basicInfoDto.setUserSign(userBasicInfoDto.getUserSign());
//        return basicInfoDto;
//    }

    public static UserBasicDTO convertEntityToDTO(UserBaseInfoEntity userBasicInfoEntity) {
        UserBasicDTO basicInfoDto = new UserBasicDTO();
        basicInfoDto.setAge(userBasicInfoEntity.getAge());
        basicInfoDto.setAvatar(userBasicInfoEntity.getAvatar());
        basicInfoDto.setSex(userBasicInfoEntity.getSex());
        basicInfoDto.setUid(userBasicInfoEntity.getUid());
        basicInfoDto.setOid(userBasicInfoEntity.getOid());
        basicInfoDto.setAid(convertEntityGetAid(userBasicInfoEntity.getUid()));
        // TODO: 2020/11/9
//        basicInfoDto.setOpenId(userBasicInfoEntity.getOpenId());
        basicInfoDto.setNickName(userBasicInfoEntity.getNickName());
        basicInfoDto.setLoginType(userBasicInfoEntity.getLoginType());
        basicInfoDto.setUserSign(userBasicInfoEntity.getUserSign());

        basicInfoDto.setLoginType(userBasicInfoEntity.getLoginType());
        basicInfoDto.setLocation(userBasicInfoEntity.getLocation());
        basicInfoDto.setBirthday(userBasicInfoEntity.getBirthday());
        basicInfoDto.setAddress(userBasicInfoEntity.getAddress());
        basicInfoDto.setEmail(userBasicInfoEntity.getEmail());
        // 这点计算星座
        basicInfoDto.setConstellation(convertEntityGetConstellation(userBasicInfoEntity.getBirthday(),
                userBasicInfoEntity.getLocation()));
        return basicInfoDto;
    }
}
