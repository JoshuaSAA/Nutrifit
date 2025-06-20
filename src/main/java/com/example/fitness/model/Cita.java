package com.example.fitness.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Cita {
    private int idCita;
    private LocalDate fechaCita;
    private LocalTime horaCita;
    private int idPaciente;
    private Paciente paciente; // Para relación
    private String observaciones;
    private String estatus;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaModificacion;

    // Constructor vacío
    public Cita() {
        this.estatus = "Programada";
        this.fechaRegistro = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
    }

    // Constructor con datos básicos
    public Cita(LocalDate fechaCita, LocalTime horaCita, int idPaciente) {
        this();
        this.fechaCita = fechaCita;
        this.horaCita = horaCita;
        this.idPaciente = idPaciente;
    }

    // Constructor completo
    public Cita(LocalDate fechaCita, LocalTime horaCita, int idPaciente, String observaciones) {
        this(fechaCita, horaCita, idPaciente);
        this.observaciones = observaciones;
    }

    // Getters y Setters
    public int getIdCita() { return idCita; }
    public void setIdCita(int idCita) { this.idCita = idCita; }

    public LocalDate getFechaCita() { return fechaCita; }
    public void setFechaCita(LocalDate fechaCita) { this.fechaCita = fechaCita; }

    public LocalTime getHoraCita() { return horaCita; }
    public void setHoraCita(LocalTime horaCita) { this.horaCita = horaCita; }

    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
        if (paciente != null) {
            this.idPaciente = paciente.getIdPaciente();
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
    public String getFechaHoraFormateada() {
        if (fechaCita != null && horaCita != null) {
            return fechaCita.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    " " + horaCita.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return "";
    }

    public boolean esProgramada() {
        return "Programada".equalsIgnoreCase(estatus);
    }

    public boolean esCompletada() {
        return "Completada".equalsIgnoreCase(estatus);
    }

    public boolean esCancelada() {
        return "Cancelada".equalsIgnoreCase(estatus);
    }

    public boolean esHoy() {
        return fechaCita != null && fechaCita.equals(LocalDate.now());
    }

    public boolean esPasada() {
        if (fechaCita == null) return false;
        LocalDate hoy = LocalDate.now();
        return fechaCita.isBefore(hoy) ||
                (fechaCita.equals(hoy) && horaCita != null && horaCita.isBefore(LocalTime.now()));
    }

    @Override
    public String toString() {
        return getFechaHoraFormateada() + " - " +
                (paciente != null ? paciente.getNombreCompleto() : "Paciente ID: " + idPaciente);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cita cita = (Cita) obj;
        return idCita == cita.idCita;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idCita);
    }
}