package com.ecommerce.model;

public class OrderItem {
    private String productoId;
    private int cantidad;
    private double precioUnitario;

    public OrderItem() {}

    public OrderItem(String productoId, int cantidad, double precioUnitario) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public String getProductoId()      { return productoId; }
    public int    getCantidad()        { return cantidad; }
    public double getPrecioUnitario()  { return precioUnitario; }

    public void setProductoId(String productoId)       { this.productoId = productoId; }
    public void setCantidad(int cantidad)              { this.cantidad = cantidad; }
    public void setPrecioUnitario(double precioUnit)   { this.precioUnitario = precioUnit; }

    public double getSubtotal() {
        return cantidad * precioUnitario;
    }

    @Override
    public String toString() {
        return String.format("Producto: %s  x%d  $%.2f c/u  Subtotal: $%.2f",
                productoId, cantidad, precioUnitario, getSubtotal());
    }
}
