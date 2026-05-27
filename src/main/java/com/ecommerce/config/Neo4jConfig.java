package com.ecommerce.config;

import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

public class Neo4jConfig implements AutoCloseable {

    private static Neo4jConfig instance;
    private final Driver driver;

    // Configurar con variables de entorno o valores por defecto
    private static final String URI      = System.getenv().getOrDefault("NEO4J_URI",      "bolt://localhost:7687");
    private static final String USER     = System.getenv().getOrDefault("NEO4J_USER",     "neo4j");
    private static final String PASSWORD = System.getenv().getOrDefault("NEO4J_PASSWORD", "password");

    private Neo4jConfig() {
        driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD));
        try {
            driver.verifyConnectivity();
            System.out.println("✓ Conexión a Neo4j establecida (" + URI + ")");
        } catch (ServiceUnavailableException e) {
            System.err.println("✗ No se pudo conectar a Neo4j en: " + URI);
            System.err.println("  Verifique que Neo4j esté corriendo y que las credenciales sean correctas.");
            System.err.println("  Puede configurar: NEO4J_URI, NEO4J_USER, NEO4J_PASSWORD");
            throw e;
        }
    }

    public static Neo4jConfig getInstance() {
        if (instance == null) {
            instance = new Neo4jConfig();
        }
        return instance;
    }

    public Driver getDriver() {
        return driver;
    }

    public Session getSession() {
        return driver.session();
    }

    @Override
    public void close() {
        if (driver != null) {
            driver.close();
            instance = null;
        }
    }
}
