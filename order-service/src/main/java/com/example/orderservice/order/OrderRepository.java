package com.example.orderservice.order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {
    private final Map<String, Order> store = new ConcurrentHashMap<String, Order>();

    public Order create(String customerId, java.math.BigDecimal amount) {
        String id = UUID.randomUUID().toString();
        Order o = new Order(id, customerId, amount, "CREATED", Instant.now());
        store.put(id, o);
        return o;
    }

    public Optional<Order> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Order> findAll() {
        return new ArrayList<Order>(store.values());
    }
}

