package com.ecommerce.model;

import java.util.List;

public class Order {
    private String id;
    private String fecha;
    private String estado;          // COMPLETADO | PENDIENTE | ENVIADO | CANCELADO
    private double montoTotal;
    private String metodoPago;
    private String direccionEnvio;
    private String usuarioId;
    private List<OrderItem> items;

    public Order() {}

    public Order(String id, String fecha, String estado, double montoTotal,
                 String metodoPago, String direccionEnvio, String usuarioId) {
        this.id = id;
        this.fecha = fecha;
        this.estado = estado;
        this.montoTotal = montoTotal;
        this.metodoPago = metodoPago;
        this.direccionEnvio = direccionEnvio;
        this.usuarioId = usuarioId;
    }

    public String getId()              { return id; }
    public String getFecha()           { return fecha; }
    public String getEstado()          { return estado; }
    public double getMontoTotal()      { return montoTotal; }
    public String getMetodoPago()      { return metodoPago; }
    public String getDireccionEnvio()  { return direccionEnvio; }
    public String getUsuarioId()       { return usuarioId; }
    public List<OrderItem> getItems()  { return items; }

    public void setId(String id)                   { this.id = id; }
    public void setFecha(String fecha)             { this.fecha = fecha; }
    public void setEstado(String estado)           { this.estado = estado; }
    public void setMontoTotal(double montoTotal)   { this.montoTotal = montoTotal; }
    public void setMetodoPago(String metodoPago)   { this.metodoPago = metodoPago; }
    public void setDireccionEnvio(String dir)      { this.direccionEnvio = dir; }
    public void setUsuarioId(String usuarioId)     { this.usuarioId = usuarioId; }
    public void setItems(List<OrderItem> items)    { this.items = items; }

    @Override
    public String toString() {
        return String.format("[%s] %s  Estado: %-12s  Total: $%8.2f  Pago: %s",
                id, fecha, estado, montoTotal, metodoPago);
    }
}
