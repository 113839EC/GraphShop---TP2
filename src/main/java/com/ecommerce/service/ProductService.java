package com.ecommerce.service;

import com.ecommerce.repository.ProductRepository;

import java.util.List;
import java.util.Map;

public class ProductService {

    private final ProductRepository repo;

    public ProductService() {
        this.repo = new ProductRepository();
    }

    public List<Map<String, Object>> listarConCategoria()         { return repo.getAllProductsWithCategory(); }
    public List<Map<String, Object>> listarPorCategoria(String id){ return repo.getProductsByCategory(id); }
    public List<Map<String, Object>> listarPorPrecio(double min, double max) {
        return repo.getProductsByPriceRange(min, max);
    }
    public List<Map<String, Object>> topVendidos(int limit)       { return repo.getTopSellingProducts(limit); }
    public List<Map<String, Object>> vieronPeroNoCompraron(String pid) {
        return repo.getUsersWhoViewedButNotPurchased(pid);
    }
}
