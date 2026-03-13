package com.example.orderservice.monitor;

import com.example.orderservice.order.Order;
import com.example.orderservice.order.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OrderMonitorScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OrderMonitorScheduler.class);

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("10000");
    private static final int HIGH_COUNT_THRESHOLD = 100;

    private final AtomicInteger errorCycle = new AtomicInteger(0);

    private final OrderRepository orderRepository;

    public OrderMonitorScheduler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * 每 30 秒執行一次，檢查訂單狀態並輸出警告。
     */
    @Scheduled(fixedRate = 30_000)
    public void monitorOrders() {
        logger.debug("OrderMonitor: starting periodic check.");

        List<Order> orders = orderRepository.findAll();
        int total = orders.size();

        logger.debug("OrderMonitor: fetched {} order(s) from repository.", total);

        if (total == 0) {
            logger.warn("OrderMonitor: no orders found in repository, system may not be receiving traffic.");
            return;
        }

        // 檢查訂單總數是否過高
        if (total >= HIGH_COUNT_THRESHOLD) {
            logger.warn("OrderMonitor: order count is high — total={}, threshold={}", total, HIGH_COUNT_THRESHOLD);
        }

        // 檢查是否有大額訂單
        long highAmountCount = orders.stream()
                .filter(o -> o.getAmount() != null && o.getAmount().compareTo(HIGH_AMOUNT_THRESHOLD) > 0)
                .count();

        logger.debug("OrderMonitor: high-amount order scan complete — found={}, threshold={}", highAmountCount, HIGH_AMOUNT_THRESHOLD);

        if (highAmountCount > 0) {
            logger.warn("OrderMonitor: detected {} high-amount order(s) exceeding threshold={}",
                    highAmountCount, HIGH_AMOUNT_THRESHOLD);
        }

        // 檢查是否有停滯在 CREATED 的訂單（未進入後續流程）
        long stuckOrders = orders.stream()
                .filter(o -> "CREATED".equals(o.getStatus()))
                .count();

        logger.debug("OrderMonitor: stuck-order scan complete — stuckCreated={}", stuckOrders);

        if (stuckOrders > 0) {
            logger.warn("OrderMonitor: {} order(s) still in CREATED status — possible processing delay. total={}",
                    stuckOrders, total);
        }

        // 當所有訂單都卡在 CREATED 時，視為異常
        if (stuckOrders == total) {
            logger.error("OrderMonitor: ALL {} order(s) are stuck in CREATED status — order processing pipeline may be down!",
                    total);
        }

        // 模擬大額訂單佔比過高時視為風險異常
        if (total > 0 && highAmountCount * 100 / total >= 80) {
            logger.error("OrderMonitor: high-amount orders exceed 80% of total — possible pricing or data anomaly. highAmount={}, total={}",
                    highAmountCount, total);
        }

        logger.warn("OrderMonitor: periodic check done — total={}, highAmount={}, stuckCreated={}",
                total, highAmountCount, stuckOrders);

        logger.debug("OrderMonitor: check cycle finished.");
    }

    /**
     * 每 20 秒模擬一次系統錯誤，供 Grafana 看板測試 ERROR 告警用。
     */
    @Scheduled(fixedRate = 20_000)
    public void simulateError() {
        int cycle = errorCycle.incrementAndGet();
        try {
            if (cycle % 3 == 0) {
                throw new IllegalStateException("payment-service connection timeout after 5000ms");
            } else if (cycle % 3 == 1) {
                throw new RuntimeException("failed to persist order — database write rejected (cycle=" + cycle + ")");
            } else {
                throw new IllegalArgumentException("invalid order state transition: CREATED -> REFUNDED (cycle=" + cycle + ")");
            }
        } catch (Exception e) {
            logger.error("OrderMonitor: simulated error triggered — cycle={}, reason={}", cycle, e.getMessage(), e);
        }
    }
}
