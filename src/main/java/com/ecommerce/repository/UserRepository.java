package com.ecommerce.repository;

import com.ecommerce.config.Neo4jConfig;
import com.ecommerce.model.User;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserRepository {

    private final Neo4jConfig config;

    public UserRepository() {
        this.config = Neo4jConfig.getInstance();
    }

    // ─── Consulta 2: Listar todos los usuarios ────────────────────────────────
    public List<User> getAllUsers() {
        String query = """
                MATCH (u:User)
                RETURN u.id AS id, u.nombre AS nombre, u.email AS email,
                       u.edad AS edad, u.ciudad AS ciudad,
                       u.fechaRegistro AS fechaRegistro,
                       u.puntosFidelidad AS puntosFidelidad
                ORDER BY u.nombre
                """;

        List<User> users = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query).list().forEach(r -> users.add(mapUser(r)));
        }
        return users;
    }

    // ─── Consulta 3: Buscar usuario por email ─────────────────────────────────
    public Optional<User> findByEmail(String email) {
        String query = """
                MATCH (u:User {email: $email})
                RETURN u.id AS id, u.nombre AS nombre, u.email AS email,
                       u.edad AS edad, u.ciudad AS ciudad,
                       u.fechaRegistro AS fechaRegistro,
                       u.puntosFidelidad AS puntosFidelidad
                """;

        try (Session session = config.getSession()) {
            Result result = session.run(query, Map.of("email", email));
            if (result.hasNext()) {
                return Optional.of(mapUser(result.next()));
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(String id) {
        String query = """
                MATCH (u:User {id: $id})
                RETURN u.id AS id, u.nombre AS nombre, u.email AS email,
                       u.edad AS edad, u.ciudad AS ciudad,
                       u.fechaRegistro AS fechaRegistro,
                       u.puntosFidelidad AS puntosFidelidad
                """;

        try (Session session = config.getSession()) {
            Result result = session.run(query, Map.of("id", id));
            if (result.hasNext()) {
                return Optional.of(mapUser(result.next()));
            }
        }
        return Optional.empty();
    }

    // ─── Consulta 4: Usuarios inactivos (nunca realizaron un pedido) ──────────
    //     Demuestra el patrón negativo de Neo4j: NOT EXISTS { subquery }
    public List<User> getInactiveUsers() {
        String query = """
                MATCH (u:User)
                WHERE NOT EXISTS {
                    MATCH (u)-[:REALIZO]->(:Order)
                }
                RETURN u.id AS id, u.nombre AS nombre, u.email AS email,
                       u.edad AS edad, u.ciudad AS ciudad,
                       u.fechaRegistro AS fechaRegistro,
                       u.puntosFidelidad AS puntosFidelidad
                ORDER BY u.fechaRegistro
                """;

        List<User> users = new ArrayList<>();
        try (Session session = config.getSession()) {
            session.run(query).list().forEach(r -> users.add(mapUser(r)));
        }
        return users;
    }

    public void createUser(User user) {
        String query = """
                MERGE (u:User {id: $id})
                SET u.nombre          = $nombre,
                    u.email           = $email,
                    u.edad            = $edad,
                    u.ciudad          = $ciudad,
                    u.fechaRegistro   = $fechaRegistro,
                    u.puntosFidelidad = $puntosFidelidad
                """;

        try (Session session = config.getSession()) {
            session.run(query, Map.of(
                    "id",              user.getId(),
                    "nombre",          user.getNombre(),
                    "email",           user.getEmail(),
                    "edad",            user.getEdad(),
                    "ciudad",          user.getCiudad(),
                    "fechaRegistro",   user.getFechaRegistro(),
                    "puntosFidelidad", user.getPuntosFidelidad()
            ));
        }
    }

    private User mapUser(org.neo4j.driver.Record r) {
        return new User(
                r.get("id").asString(),
                r.get("nombre").asString(),
                r.get("email").asString(),
                r.get("edad").asInt(),
                r.get("ciudad").asString(),
                r.get("fechaRegistro").asString(),
                r.get("puntosFidelidad").asInt()
        );
    }
}
