package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RecommendationServiceTest {
    private Map<Long, Map<Long, Integer>> marks = new HashMap<>();
    private RecommendationService recommendationService;

    @Test
    void fillMatricesTest() {
        recommendationService = new RecommendationService();
        fillMarks();
        Map<Long, Double> recommendations = recommendationService.getRecommendation(marks, 3L);
        assertEquals(recommendations.get(2L), (double)marks.get(3L).get(2L), 0.00001D, "");
    }

    private void fillMarks() {
        marks.put(1L, new HashMap<>() {{
                    put(1L, 5);
                    put(2L, 3);
                    put(3L, 2);
                }}
        );
        marks.put(2L, new HashMap<>() {{
                    put(1L, 3);
                    put(2L, 4);
                }}
        );
        marks.put(3L, new HashMap<>() {{
                    put(2L, 2);
                    put(3L, 5);
                }}
        );
    }
}
