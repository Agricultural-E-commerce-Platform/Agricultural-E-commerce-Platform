package com.spartafarmer.agri_commerce.domain.product.service;

import com.spartafarmer.agri_commerce.domain.product.dto.PopularKeywordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    public List<PopularKeywordResponse> getTopKeywords() {
        Set<ZSetOperations.TypedTuple<String>> result =
                redisTemplate.opsForZSet().reverseRangeWithScores(KEY, 0, 9);

        // Redis 결과가 없으면 빈 리스트 반환
        if (result == null || result.isEmpty()) {
            return List.of();
        }

        List<PopularKeywordResponse> list = new ArrayList<>();
        int rank = 1;

        for (ZSetOperations.TypedTuple<String> tuple : result) {
            // score가 null일 가능성 방어
            long searchCount = tuple.getScore() == null ? 0L : tuple.getScore().longValue();

            list.add(new PopularKeywordResponse(
                    rank++,              // 순위
                    tuple.getValue(),    // 검색어
                    searchCount          // 검색 횟수
            ));
        }

        return list;
    }
}