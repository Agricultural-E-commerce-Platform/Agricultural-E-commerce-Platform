package com.spartafarmer.agri_commerce.domain.product.service;

import com.spartafarmer.agri_commerce.domain.product.dto.PopularKeywordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PopularSearchService {

    private final StringRedisTemplate redisTemplate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 날짜별 인기검색어 ZSET key
    private String getRankingKey() {
        return "popular:keywords:" + LocalDate.now().format(DATE_FORMATTER);
    }

    // 날짜별 + 키워드별 중복방지 SET key
    private String getUserSetKey(String keyword) {
        return "popular:users:" + LocalDate.now().format(DATE_FORMATTER) + ":" + keyword;
    }

    // 회원당 동일 검색어 1회만 집계
    public void increaseKeyword(Long userId, String keyword) {
        String rankingKey = getRankingKey();
        String userSetKey = getUserSetKey(keyword);

        // SET에 userId가 처음 들어가면 1, 이미 있으면 0 반환
        Long added = redisTemplate.opsForSet().add(userSetKey, String.valueOf(userId));

        // 처음 검색한 경우에만 ZSET 점수 증가
        if (added != null && added > 0) {
            redisTemplate.opsForZSet().incrementScore(rankingKey, keyword, 1);
        }
    }

    // 인기 검색어 TOP 10 조회
    public List<PopularKeywordResponse> getTopKeywords() {
        String rankingKey = getRankingKey();

        Set<ZSetOperations.TypedTuple<String>> result =
                redisTemplate.opsForZSet().reverseRangeWithScores(rankingKey, 0, 9);

        if (result == null || result.isEmpty()) {
            return List.of();
        }

        List<PopularKeywordResponse> list = new ArrayList<>();
        int rank = 1;

        for (ZSetOperations.TypedTuple<String> tuple : result) {
            long searchCount = tuple.getScore() == null ? 0L : tuple.getScore().longValue();

            list.add(new PopularKeywordResponse(
                    rank++,
                    tuple.getValue(),
                    searchCount
            ));
        }

        return list;
    }

    // 매일 00시 초기화용
    public void clearTodayKeywords() {
        String today = LocalDate.now().format(DATE_FORMATTER);

        String rankingKey = "popular:keywords:" + today;
        redisTemplate.delete(rankingKey);

        // keyword별 userSetKey는 패턴 삭제 필요
        Set<String> userKeys = redisTemplate.keys("popular:users:" + today + ":*");
        if (userKeys != null && !userKeys.isEmpty()) {
            redisTemplate.delete(userKeys);
        }
    }
}