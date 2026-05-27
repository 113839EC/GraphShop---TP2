package com.ecommerce.service;

import com.ecommerce.repository.RecommendationRepository;

import java.util.List;
import java.util.Map;

public class RecommendationService {

    private final RecommendationRepository repo;

    public RecommendationService() {
        this.repo = new RecommendationRepository();
    }

    public List<Map<String, Object>> recomendacionesColaborativas(String userId) {
        return repo.getCollaborativeRecommendations(userId);
    }

    public List<Map<String, Object>> usuariosSimilares(String userId) {
        return repo.getSimilarUsersByJaccard(userId);
    }

    public List<Map<String, Object>> vistosNoCamprados(String userId) {
        return repo.getViewedButNotPurchasedByUser(userId);
    }
}
