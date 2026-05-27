package com.ecommerce.repository;

import com.ecommerce.config.Neo4jConfig;
import com.ecommerce.model.Category;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryRepository {

    private final Neo4jConfig config;

    public CategoryRepository() {
        this.config = Neo4jConfig.getInstance();
    }

    // ─── Consulta 1: Listar todas las categorias ─────────────────────────────
    public List<Category> getAllCategories() {
        String query = """
                MATCH (c:Category)
                RETURN c.id AS id, c.nombre AS nombre, c.descripcion AS descripcion
                ORDER BY c.nombre
                """;

        List<Category> categories = new ArrayList<>();
        try (Session session = config.getSession()) {
            Result result = session.run(query);
            result.list().forEach(record -> categories.add(new Category(
                    record.get("id").asString(),
                    record.get("nombre").asString(),
                    record.get("descripcion").asString()
            )));
        }
        return categories;
    }

    public void createCategory(Category category) {
        String query = """
                MERGE (c:Category {id: $id})
                SET c.nombre = $nombre,
                    c.descripcion = $descripcion
                """;
        try (Session session = config.getSession()) {
            session.run(query, Map.of(
                    "id",          category.getId(),
                    "nombre",      category.getNombre(),
                    "descripcion", category.getDescripcion()
            ));
        }
    }
}
