package com.example.fitness.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public class Paciente {
    private int idPaciente;
    private String nombre;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String genero;
    private BigDecimal peso;
    private BigDecimal estatura;
    private String talla;
    private String email;
    private String telefono;
    private String domicilio;
    private byte[] fotografia;
    private int idMedico;
    private Medico medico; // Para relación
    private String estatus;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaModificacion;

    // Constructor vacío
    public Paciente() {
        this.estatus = "Activo";
        this.fechaRegistro = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
    }

    // Constructor con datos básicos
    public Paciente(String nombre, String apellidos, LocalDate fechaNacimiento,
                    String genero, String email, String telefono, int idMedico) {
        this();
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.email = email;
        this.telefono = telefono;
        this.idMedico = idMedico;
    }

    // Getters y Setters
    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }

    public BigDecimal getEstatura() { return estatura; }
    public void setEstatura(BigDecimal estatura) { this.estatura = estatura; }

    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDomicilio() { return domicilio; }
    public void setDomicilio(String domicilio) { this.domicilio = domicilio; }

    public byte[] getFotografia() { return fotografia; }
    public void setFotografia(byte[] fotografia) { this.fotografia = fotografia; }

    public int getIdMedico() { return idMedico; }
    public void setIdMedico(int idMedico) { this.idMedico = idMedico; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) {
        this.medico = medico;
        if (medico != null) {
            this.idMedico = medico.getIdMedico();
        }
    }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    // Métodos útiles
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    public int getEdad() {
        if (fechaNacimiento != null) {
            return Period.between(fechaNacimiento, LocalDate.now()).getYears();
        }
        return 0;
    }

    public BigDecimal calcularIMC() {
        if (peso != null && estatura != null && estatura.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal imc = peso.divide(estatura.multiply(estatura), 2, RoundingMode.HALF_UP);
            return imc;
        }
        return BigDecimal.ZERO;
    }

    public String getClasificacionIMC() {
        BigDecimal imc = calcularIMC();
        if (imc.compareTo(BigDecimal.ZERO) == 0) return "No calculado";
        if (imc.compareTo(new BigDecimal("18.5")) < 0) return "Bajo peso";
        if (imc.compareTo(new BigDecimal("25")) < 0) return "Peso normal";
        if (imc.compareTo(new BigDecimal("30")) < 0) return "Sobrepeso";
        return "Obesidad";
    }

    public boolean estaActivo() {
        return "Activo".equalsIgnoreCase(estatus);
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " - " + email;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Paciente paciente = (Paciente) obj;
        return idPaciente == paciente.idPaciente;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idPaciente);
    }
}