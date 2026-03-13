package com.example.springbootapp.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CpuLoadScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CpuLoadScheduler.class);

    // 可調整參數：每次排程要跑多少次迴圈
    private static final int OUTER_LOOP_COUNT = 10_000;
    private static final int INNER_LOOP_COUNT = 50_000;

    /**
     * 每 10 秒執行一次，模擬 CPU 密集運算。
     */
    @Scheduled(fixedRate = 10_000)
    public void generateCpuLoad() {
        logger.info("CPU load task started.");
        long dummy = 0L;
        for (int i = 0; i < OUTER_LOOP_COUNT; i++) {
            for (int j = 0; j < INNER_LOOP_COUNT; j++) {
                // 做一些數學運算以佔用 CPU
                dummy += (i * 31L + j * 17L) ^ 13L;
            }
        }
        // 防止 JIT 完全優化掉計算
        logger.info("CPU load task finished. dummy={}", dummy);
    }
}

