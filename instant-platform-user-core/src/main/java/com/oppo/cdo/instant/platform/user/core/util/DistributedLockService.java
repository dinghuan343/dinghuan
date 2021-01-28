package com.oppo.cdo.instant.platform.user.core.util;

import com.oppo.cdo.instant.platform.common.redis.manager.RedisClusterManager;
import com.oppo.cdo.instant.platform.common.redis.util.RedisLockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * description:
 *
 * @author 80229032 chenchangjiang
 * @date 2019/12/30 15:12
 */
@Component
public class DistributedLockService {
    @Autowired
    @Qualifier("instantGameRedisClusterManager")
    private RedisClusterManager redisClusterManager;
    
    /**
     *
     * @param lockKey 锁主键
     * @param lockVal 锁标识
     * @param expireTime 锁过期时间，milliseconds
     * @return
     */
    public boolean tryLock(String lockKey, String lockVal, int expireTime) {
        return RedisLockUtil.tryGetDistributedLock(redisClusterManager, lockKey, lockVal, expireTime);
    }
    
    /**
     *
     * @param lockKey 锁主键
     * @param lockVal 锁标识
     * @return
     */
    public boolean releaseLock(String lockKey, String lockVal) {
        return RedisLockUtil.releaseDistributedLock(redisClusterManager, lockKey, lockVal);
    }
    
    /**
     *
     * @param lockKey 锁主键
     * @param lockVal 锁标识
     * @return
     */
    public boolean blockSet(String lockKey, String lockVal) {
        return RedisLockUtil.blockSet(redisClusterManager, lockKey, lockVal);
    }
}
