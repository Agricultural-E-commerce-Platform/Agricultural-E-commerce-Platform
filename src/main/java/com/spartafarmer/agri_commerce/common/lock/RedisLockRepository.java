package com.spartafarmer.agri_commerce.common.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RedisLockRepository {

    private final StringRedisTemplate redisTemplate;

    // 락 해제
    private static final RedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            // Lua Script (Redis 가 지원하는 언어로 아래 스크립트가 끝날 때까지 다른 요청을 막음)
            // 명령어를 두 개로 나눠 보내면 중간에 다른 스레드가 끼어들 수 있기 때문에 묶어서 실행
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "    return redis.call('del', KEYS[1]) " +
                    "else " +
                    "    return 0 " +
                    "end",
            Long.class
    );

    // 락 걸기 (key: 락 이름, value: 락 소유자 식별값(예: UUID), ttl: 락 유지 시간)
    public boolean tryLock(String key, String value, Duration ttl) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, value, ttl);          // key가 없을 때만 저장 -> 락이 없을 때만 락 걸기
        return Boolean.TRUE.equals(success);            // 성공시 true 반환 실패시 false(null이어도 false) 반환
    }

    // 락 풀기
    public void unlock(String key, String value) {
        // exec 명령으로 위 Lua Script 실행시키기
        redisTemplate.execute(
                UNLOCK_SCRIPT,  // Lua Script 실행
                List.of(key),   // 락 이름
                value           // 락 소유자 식별값 (UUID)
        );
    }
}