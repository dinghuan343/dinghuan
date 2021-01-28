package com.oppo.cdo.instant.platform.user.core.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.oppo.basic.heracles.client.core.spring.annotation.HeraclesConfigUpdateListener;
import com.oppo.cdo.instant.platform.common.redis.manager.RedisClusterManager;
import com.oppo.cdo.instant.platform.user.core.convertor.UserInfoConvertor;
import com.oppo.cdo.instant.platform.user.core.util.CacheKeyUtil;
import com.oppo.cdo.instant.platform.user.domain.dto.rsp.UserBasicDTO;
import com.oppo.framework.cache0.CacheEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class HeraclesAIUsersConfig {

    @Value("${AIUser.json}")
    private String                    AIUserJson;

    @Autowired
    private RedisClusterManager       redisClusterManager;

    /**
     * OID的隐射关系
     */
    public static final Integer       SUB_OID = 901234567;

    private Map<String, UserBasicDTO> AI      = new HashMap<String, UserBasicDTO>();

    private void setValue() {
        Gson gson = new Gson();
        List<UserBasicDTO> list = gson.fromJson(AIUserJson, new TypeToken<List<UserBasicDTO>>() {
        }.getType());

        AI = list.stream().collect(Collectors.toMap(UserBasicDTO::getUid, temp -> {
            temp.setOid(String.valueOf(Integer.parseInt(temp.getUid()) - SUB_OID));
            temp.setConstellation(UserInfoConvertor.convertEntityGetConstellation(temp.getBirthday(),
                                                                                  temp.getLocation()));
            
            String cacheKey = CacheKeyUtil.getUserBasicCacheKey(temp.getUid());
            CacheEntity entity = new CacheEntity(temp);
            entity.setTs(System.currentTimeMillis());
            redisClusterManager.setObject(cacheKey, entity);
            
            return temp;
        }));
    }

    @PostConstruct
    public void initConfig() {
        setValue();
    }

    @HeraclesConfigUpdateListener(fileName = "AIUser.json")
    public void change(String newV, String old) {
        if (StringUtils.isNotBlank(newV)) {
            AIUserJson = newV;
            setValue();
        }
    }

    public Map<String, UserBasicDTO> getAI() {
        return AI;
    }

    public UserBasicDTO getAIByUid(String uid) {
        UserBasicDTO infoDto = AI.get(uid);
        return infoDto;
    }

}
