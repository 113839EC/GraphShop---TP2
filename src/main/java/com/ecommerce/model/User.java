package com.ecommerce.model;

public class User {
    private String id;
    private String nombre;
    private String email;
    private int edad;
    private String ciudad;
    private String fechaRegistro;
    private int puntosFidelidad;

    public User() {}

    public User(String id, String nombre, String email, int edad,
                String ciudad, String fechaRegistro, int puntosFidelidad) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.edad = edad;
        this.ciudad = ciudad;
        this.fechaRegistro = fechaRegistro;
        this.puntosFidelidad = puntosFidelidad;
    }

    public String getId()              { return id; }
    public String getNombre()          { return nombre; }
    public String getEmail()           { return email; }
    public int    getEdad()            { return edad; }
    public String getCiudad()          { return ciudad; }
    public String getFechaRegistro()   { return fechaRegistro; }
    public int    getPuntosFidelidad() { return puntosFidelidad; }

    public void setId(String id)                         { this.id = id; }
    public void setNombre(String nombre)                 { this.nombre = nombre; }
    public void setEmail(String email)                   { this.email = email; }
    public void setEdad(int edad)                        { this.edad = edad; }
    public void setCiudad(String ciudad)                 { this.ciudad = ciudad; }
    public void setFechaRegistro(String fechaRegistro)   { this.fechaRegistro = fechaRegistro; }
    public void setPuntosFidelidad(int puntosFidelidad)  { this.puntosFidelidad = puntosFidelidad; }

    @Override
    public String toString() {
        return String.format("[%s] %-25s %-35s %s  Puntos: %d",
                id, nombre, email, ciudad, puntosFidelidad);
    }
}
