package com.oppo.cdo.instant.platform.user.core.util;

import com.oppo.cdo.instant.platform.user.domain.dto.req.UpdateUserInfoReqDto;
import com.oppo.cdo.instant.platform.user.domain.dto.req.UserRegisterReqDto;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.LoginRspDto;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UserBasicDTO;
import com.oppo.cdo.instant.platform.user.entity.UserBaseInfoEntity;
import com.oppo.cdo.instant.platform.user.entity.UserOauthInfoEntity;

public class BeanToLoginRspDto {

    public static UserBaseInfoEntity convertUserUpdateToBaseEntity(UpdateUserInfoReqDto reqDto) {
        UserBaseInfoEntity entity = new UserBaseInfoEntity();
        entity.setLocation(reqDto.getLocation());
        entity.setAge(reqDto.getAge());
        entity.setAvatar(reqDto.getAvatar());
        entity.setNickName(reqDto.getNickName());
        entity.setBirthday(reqDto.getBirthday());
        entity.setUserSign(reqDto.getUserSign());
        entity.setAddress(reqDto.getAddress());
        entity.setSex(reqDto.getSex());
        entity.setUid(reqDto.getUid());
        return entity;
    }

    public static UserOauthInfoEntity convertUserOauthEntity(UserRegisterReqDto reqDto) {
        UserOauthInfoEntity entity = new UserOauthInfoEntity();
        entity.setLocation(reqDto.getLocation());
        entity.setOpenId(reqDto.getOpenId());
        entity.setAccessToken(reqDto.getToken());
        entity.setExpires(reqDto.getExpires());
        entity.setLoginType(reqDto.getLoginType());
        entity.setUid(reqDto.getUid());
        return entity;
    }

    public static UserBaseInfoEntity convertUserRegisterToBaseEntity(UserRegisterReqDto reqDto) {
        UserBaseInfoEntity entity = new UserBaseInfoEntity();
        entity.setLocation(reqDto.getLocation());
        entity.setAge(reqDto.getAge());
        entity.setAvatar(reqDto.getAvatar());
        entity.setNickName(reqDto.getNickName());
        entity.setBirthday(reqDto.getBirthday());
        entity.setSex(reqDto.getSex());
        entity.setLoginType(reqDto.getLoginType());
        entity.setUid(reqDto.getUid());
        entity.setOid(reqDto.getOid());
        entity.setAid(reqDto.getAid());
        entity.setOpenId(reqDto.getOpenId());
        entity.setAddress(reqDto.getAddress());
        entity.setEmail(reqDto.getEmail());
        return entity;
    }

//    public static UserBasicInfoDto convertUserEntityToDto(UserBaseInfoEntity userBaseInfoEntity) {
//
//        return UserInfoConvertor.convertEntityToDto(userBaseInfoEntity);
//    }

    public static LoginRspDto convertEntityToRsp(UserBasicDTO userBasicInfoEntity, UserOauthInfoEntity  oauthInfoEntity) {
        LoginRspDto rspDto = new LoginRspDto();
        rspDto.setAvatar(userBasicInfoEntity.getAvatar());
        rspDto.setNickName(userBasicInfoEntity.getNickName());
        rspDto.setAge(userBasicInfoEntity.getAge());
        rspDto.setBirthday(userBasicInfoEntity.getBirthday());
        rspDto.setSex(userBasicInfoEntity.getSex());
        rspDto.setLocation(userBasicInfoEntity.getLocation());
        rspDto.setUid(userBasicInfoEntity.getUid());
        rspDto.setOid(userBasicInfoEntity.getOid());
        // 登录有可能传了空对象进来，下面这个要放到其他地方处理
        // rspDto.setAid(UserInfoConvertor.convertEntityGetAid(userBasicInfoEntity.getUid()));
        rspDto.setOpenId((oauthInfoEntity.getOpenId()));
        rspDto.setUserSign(userBasicInfoEntity.getUserSign());
        rspDto.setLoginType(userBasicInfoEntity.getLoginType());
        rspDto.setAddress(userBasicInfoEntity.getAddress());
        rspDto.setAccessToken(oauthInfoEntity.getAccessToken());
        rspDto.setExpires(oauthInfoEntity.getExpires());
        rspDto.setEmail(userBasicInfoEntity.getEmail());
        return rspDto;
    }

    public static UserRegisterReqDto constructUserRegisterReqDto(LoginRspDto loginRspDto, String uid, String aid) {
        UserRegisterReqDto userRegisterReqDto = new UserRegisterReqDto();
        userRegisterReqDto.setLoginType(loginRspDto.getLoginType());
        userRegisterReqDto.setLocation(loginRspDto.getLocation());
        userRegisterReqDto.setOpenId(loginRspDto.getOpenId());
        userRegisterReqDto.setToken((loginRspDto.getAccessToken()));
        userRegisterReqDto.setAge(loginRspDto.getAge());
        userRegisterReqDto.setAvatar(loginRspDto.getAvatar());
        userRegisterReqDto.setBirthday(loginRspDto.getBirthday());
        userRegisterReqDto.setAddress(loginRspDto.getAddress());
        userRegisterReqDto.setSex(loginRspDto.getSex());
        userRegisterReqDto.setNickName(loginRspDto.getNickName());
        userRegisterReqDto.setEmail(loginRspDto.getEmail());
        userRegisterReqDto.setUid(uid);
        userRegisterReqDto.setOid(uid);
        userRegisterReqDto.setAid(aid);
        userRegisterReqDto.setExpires(loginRspDto.getExpires());
        return userRegisterReqDto;
    }

    public static LoginRspDto covertToLoginRspFromDto(UserBasicDTO basicInfoDto) {
        LoginRspDto loginRspDto = new LoginRspDto();
        loginRspDto.setSex(basicInfoDto.getSex());
        loginRspDto.setUid(basicInfoDto.getUid());
        loginRspDto.setOid(basicInfoDto.getOid());
        loginRspDto.setAid(basicInfoDto.getAid());
        loginRspDto.setAvatar(basicInfoDto.getAvatar());
        loginRspDto.setLocation(basicInfoDto.getLocation());
        loginRspDto.setBirthday(basicInfoDto.getBirthday());
        loginRspDto.setAge(basicInfoDto.getAge());
        loginRspDto.setEmail(basicInfoDto.getEmail());
        loginRspDto.setNickName(basicInfoDto.getNickName());
        loginRspDto.setUserSign(basicInfoDto.getUserSign());
        loginRspDto.setAddress(basicInfoDto.getAddress());
        loginRspDto.setLoginType((basicInfoDto.getLoginType()));
        return loginRspDto;
    }
}
