package com.example.fitness.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Consulta {
    private int idConsulta;
    private int idPaciente;
    private Paciente paciente; // Para relación
    private LocalDate fechaConsulta;
    private LocalTime horaConsulta;
    private BigDecimal pesoActual;
    private String tallaActual;
    private BigDecimal imc;
    private int idDieta;
    private Dieta dieta; // Para relación
    private String observaciones;
    private String estatus;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaModificacion;

    // Constructor vacío
    public Consulta() {
        this.estatus = "Activa";
        this.fechaRegistro = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
    }

    // Constructor con datos básicos
    public Consulta(int idPaciente, LocalDate fechaConsulta, LocalTime horaConsulta) {
        this();
        this.idPaciente = idPaciente;
        this.fechaConsulta = fechaConsulta;
        this.horaConsulta = horaConsulta;
    }

    // Getters y Setters
    public int getIdConsulta() { return idConsulta; }
    public void setIdConsulta(int idConsulta) { this.idConsulta = idConsulta; }

    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
        if (paciente != null) {
            this.idPaciente = paciente.getIdPaciente();
        }
    }

    public LocalDate getFechaConsulta() { return fechaConsulta; }
    public void setFechaConsulta(LocalDate fechaConsulta) { this.fechaConsulta = fechaConsulta; }

    public LocalTime getHoraConsulta() { return horaConsulta; }
    public void setHoraConsulta(LocalTime horaConsulta) { this.horaConsulta = horaConsulta; }

    public BigDecimal getPesoActual() { return pesoActual; }
    public void setPesoActual(BigDecimal pesoActual) {
        this.pesoActual = pesoActual;
        calcularIMC(); // Recalcular IMC cuando cambia el peso
    }

    public String getTallaActual() { return tallaActual; }
    public void setTallaActual(String tallaActual) { this.tallaActual = tallaActual; }

    public BigDecimal getImc() { return imc; }
    public void setImc(BigDecimal imc) { this.imc = imc; }

    public int getIdDieta() { return idDieta; }
    public void setIdDieta(int idDieta) { this.idDieta = idDieta; }

    public Dieta getDieta() { return dieta; }
    public void setDieta(Dieta dieta) {
        this.dieta = dieta;
        if (dieta != null) {
            this.idDieta = dieta.getIdDieta();
        }
    }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    // Métodos útiles
    public void calcularIMC() {
        if (pesoActual != null && paciente != null && paciente.getEstatura() != null) {
            BigDecimal estatura = paciente.getEstatura();
            if (estatura.compareTo(BigDecimal.ZERO) > 0) {
                this.imc = pesoActual.divide(estatura.multiply(estatura), 2, RoundingMode.HALF_UP);
            }
        }
    }

    public String getClasificacionIMC() {
        if (imc == null || imc.compareTo(BigDecimal.ZERO) == 0) return "No calculado";
        if (imc.compareTo(new BigDecimal("18.5")) < 0) return "Bajo peso";
        if (imc.compareTo(new BigDecimal("25")) < 0) return "Peso normal";
        if (imc.compareTo(new BigDecimal("30")) < 0) return "Sobrepeso";
        return "Obesidad";
    }

    public boolean esActiva() {
        return "Activa".equalsIgnoreCase(estatus);
    }

    public boolean esFinalizada() {
        return "Finalizada".equalsIgnoreCase(estatus);
    }

    public boolean esCancelada() {
        return "Cancelada".equalsIgnoreCase(estatus);
    }

    @Override
    public String toString() {
        return "Consulta " + fechaConsulta + " - " +
                (paciente != null ? paciente.getNombreCompleto() : "Paciente ID: " + idPaciente);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Consulta consulta = (Consulta) obj;
        return idConsulta == consulta.idConsulta;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idConsulta);
    }
}