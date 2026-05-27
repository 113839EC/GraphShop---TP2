package com.ecommerce.service;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.repository.OrderRepository;

import java.util.List;
import java.util.Map;

public class OrderService {

    private final OrderRepository repo;

    public OrderService() {
        this.repo = new OrderRepository();
    }

    public List<Map<String, Object>> historialDeCompras(String userId) {
        return repo.getUserPurchaseHistory(userId);
    }

    public void crearPedido(Order order, List<OrderItem> items) {
        repo.createOrder(order, items);
    }
}
