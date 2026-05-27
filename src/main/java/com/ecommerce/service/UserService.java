package com.ecommerce.service;

import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserRepository repo;

    public UserService() {
        this.repo = new UserRepository();
    }

    public List<User> listarTodos()               { return repo.getAllUsers(); }
    public Optional<User> buscarPorEmail(String e) { return repo.findByEmail(e); }
    public Optional<User> buscarPorId(String id)  { return repo.findById(id); }
    public List<User> listarInactivos()           { return repo.getInactiveUsers(); }
}
