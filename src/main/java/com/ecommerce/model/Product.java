package com.ecommerce.model;

public class Product {
    private String id;
    private String nombre;
    private String descripcion;
    private double precio;
    private int stock;
    private String marca;

    public Product() {}

    public Product(String id, String nombre, String descripcion,
                   double precio, int stock, String marca) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.marca = marca;
    }

    public String getId()          { return id; }
    public String getNombre()      { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio()      { return precio; }
    public int    getStock()       { return stock; }
    public String getMarca()       { return marca; }

    public void setId(String id)                   { this.id = id; }
    public void setNombre(String nombre)           { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecio(double precio)           { this.precio = precio; }
    public void setStock(int stock)                { this.stock = stock; }
    public void setMarca(String marca)             { this.marca = marca; }

    @Override
    public String toString() {
        return String.format("[%s] %-40s %-15s $%8.2f  Stock: %d",
                id, nombre, marca, precio, stock);
    }
}
