package com.example.fitness.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HistorialMedidas {
    private int idHistorial;
    private int idPaciente;
    private int idConsulta;
    private BigDecimal peso;
    private BigDecimal estatura;
    private String talla;
    private BigDecimal imc;
    private LocalDate fechaMedicion;
    private String observaciones;

    // Constructor vacío
    public HistorialMedidas() {}

    // Constructor con datos básicos
    public HistorialMedidas(int idPaciente, BigDecimal peso, BigDecimal estatura,
                            String talla, BigDecimal imc, LocalDate fechaMedicion) {
        this.idPaciente = idPaciente;
        this.peso = peso;
        this.estatura = estatura;
        this.talla = talla;
        this.imc = imc;
        this.fechaMedicion = fechaMedicion;
    }

    // Getters y Setters
    public int getIdHistorial() { return idHistorial; }
    public void setIdHistorial(int idHistorial) { this.idHistorial = idHistorial; }

    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }

    public int getIdConsulta() { return idConsulta; }
    public void setIdConsulta(int idConsulta) { this.idConsulta = idConsulta; }

    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }

    public BigDecimal getEstatura() { return estatura; }
    public void setEstatura(BigDecimal estatura) { this.estatura = estatura; }

    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }

    public BigDecimal getImc() { return imc; }
    public void setImc(BigDecimal imc) { this.imc = imc; }

    public LocalDate getFechaMedicion() { return fechaMedicion; }
    public void setFechaMedicion(LocalDate fechaMedicion) { this.fechaMedicion = fechaMedicion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    @Override
    public String toString() {
        return "Medidas " + fechaMedicion + " - Peso: " + peso + "kg, IMC: " + imc;
    }
}