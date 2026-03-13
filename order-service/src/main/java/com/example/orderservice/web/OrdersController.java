package com.example.orderservice.web;

import com.example.orderservice.order.CreateOrderRequest;
import com.example.orderservice.order.Order;
import com.example.orderservice.order.OrderRepository;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders")
public class OrdersController {
    private final OrderRepository repo;

    public OrdersController(OrderRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Order> list() {
        return repo.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order create(@Valid @RequestBody CreateOrderRequest req) {
        return repo.create(req.getCustomerId(), req.getAmount());
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable("id") String id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
    }
}

