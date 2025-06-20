package com.example.fitness.model;

import java.time.LocalDateTime;

public class Dieta {
    private int idDieta;
    private String nombreDieta;
    private String descripcion;
    private int caloriasTotales;
    private LocalDateTime fechaCreacion;

    // Constructor vacío
    public Dieta() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructor con datos básicos
    public Dieta(String nombreDieta, String descripcion, int caloriasTotales) {
        this();
        this.nombreDieta = nombreDieta;
        this.descripcion = descripcion;
        this.caloriasTotales = caloriasTotales;
    }

    // Getters y Setters
    public int getIdDieta() { return idDieta; }
    public void setIdDieta(int idDieta) { this.idDieta = idDieta; }

    public String getNombreDieta() { return nombreDieta; }
    public void setNombreDieta(String nombreDieta) { this.nombreDieta = nombreDieta; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getCaloriasTotales() { return caloriasTotales; }
    public void setCaloriasTotales(int caloriasTotales) { this.caloriasTotales = caloriasTotales; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    @Override
    public String toString() {
        return nombreDieta + " (" + caloriasTotales + " cal)";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Dieta dieta = (Dieta) obj;
        return idDieta == dieta.idDieta;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idDieta);
    }
}
