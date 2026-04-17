package com.spartafarmer.agri_commerce.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PopularSearchService {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY = "popular:keywords";

    // 검색어 카운트 증가
    public void increaseKeyword(String keyword) {
        redisTemplate.opsForZSet().incrementScore(KEY, keyword, 1);
    }

    // 인기 검색어 TOP 10 조회
    public List<Map<String, Object>> getTopKeywords() {
        Set<ZSetOperations.TypedTuple<String>> result =
                redisTemplate.opsForZSet().reverseRangeWithScores(KEY, 0, 9);

        List<Map<String, Object>> list = new ArrayList<>();
        int rank = 1;

        for (ZSetOperations.TypedTuple<String> tuple : result) {
            Map<String, Object> map = new HashMap<>();
            map.put("rank", rank++);
            map.put("keyword", tuple.getValue());
            map.put("searchCount", tuple.getScore().longValue());
            list.add(map);
        }

        return list;
    }
}