package org.example.service;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisDistributedLockService {

    private static final String LOCK_VALUE_PREFIX = "LOCKED_BY:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Try to acquire a distributed lock.
     *
     * @param lockKey      the redis key used as lock
     * @param ownerId      unique id of lock owner (e.g. machine + thread id)
     * @param expireMillis lock expiration in milliseconds
     * @return true if lock acquired, false otherwise
     */
    public boolean tryLock(String lockKey, String ownerId, long expireMillis) {
        String value = buildLockValue(ownerId);
        Boolean success = stringRedisTemplate
                .opsForValue()
                .setIfAbsent(lockKey, value, Duration.ofMillis(expireMillis));
        return Boolean.TRUE.equals(success);
    }

    /**
     * Block current thread and keep retrying until the lock is acquired
     * or timeout reached.
     *
     * @param lockKey              the redis key used as lock
     * @param ownerId              unique id of lock owner
     * @param expireMillis         lock expiration in milliseconds
     * @param retryIntervalMillis  sleep time between retries
     * @param timeoutMillis        max wait time in milliseconds, 0 or negative means no timeout
     * @return true if lock acquired, false if timeout occurred
     */
    public boolean lockWithRetry(String lockKey,
                                 String ownerId,
                                 long expireMillis,
                                 long retryIntervalMillis,
                                 long timeoutMillis) {
        long start = System.currentTimeMillis();
        while (true) {
            if (tryLock(lockKey, ownerId, expireMillis)) {
                return true;
            }
            if (timeoutMillis > 0) {
                long cost = System.currentTimeMillis() - start;
                if (cost >= timeoutMillis) {
                    return false;
                }
            }
            try {
                Thread.sleep(retryIntervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    /**
     * Release the distributed lock.
     * Only the same owner that acquired the lock can release it.
     *
     * @param lockKey redis key used as lock
     * @param ownerId unique id of lock owner
     * @return true if lock was released, false if lock did not exist
     *         or it is owned by someone else
     */
    public boolean unlock(String lockKey, String ownerId) {
        String expectValue = buildLockValue(ownerId);
        String currentValue = stringRedisTemplate.opsForValue().get(lockKey);
        if (currentValue == null) {
            return false;
        }
        if (!expectValue.equals(currentValue)) {
            // Lock is owned by another owner, do not delete it.
            return false;
        }
        Boolean deleted = stringRedisTemplate.delete(lockKey);
        return Boolean.TRUE.equals(deleted);
    }

    private String buildLockValue(String ownerId) {
        return LOCK_VALUE_PREFIX + ownerId;
    }
}


