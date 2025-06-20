package com.example.fitness.model;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Medico {

        private int idMedico;
        private String nombre;
        private String apellidos;
        private LocalDate fechaNacimiento;
        private String genero;
        private String domicilio;
        private String numeroPersonal;
        private String cedulaProfesional;
        private String contrasena;
        private byte[] fotografia;
        private String estatus;
        private LocalDateTime fechaRegistro;
        private LocalDateTime fechaModificacion;

        // Constructor vacío
        public Medico() {
            this.estatus = "Activo";
            this.fechaRegistro = LocalDateTime.now();
            this.fechaModificacion = LocalDateTime.now();
        }

        // Constructor completo
        public Medico(String nombre, String apellidos, LocalDate fechaNacimiento,
                      String genero, String domicilio, String numeroPersonal,
                      String cedulaProfesional, String contrasena) {
            this();
            this.nombre = nombre;
            this.apellidos = apellidos;
            this.fechaNacimiento = fechaNacimiento;
            this.genero = genero;
            this.domicilio = domicilio;
            this.numeroPersonal = numeroPersonal;
            this.cedulaProfesional = cedulaProfesional;
            this.contrasena = contrasena;
        }

        // Getters y Setters
        public int getIdMedico() { return idMedico; }
        public void setIdMedico(int idMedico) { this.idMedico = idMedico; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getApellidos() { return apellidos; }
        public void setApellidos(String apellidos) { this.apellidos = apellidos; }

        public LocalDate getFechaNacimiento() { return fechaNacimiento; }
        public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

        public String getGenero() { return genero; }
        public void setGenero(String genero) { this.genero = genero; }

        public String getDomicilio() { return domicilio; }
        public void setDomicilio(String domicilio) { this.domicilio = domicilio; }

        public String getNumeroPersonal() { return numeroPersonal; }
        public void setNumeroPersonal(String numeroPersonal) { this.numeroPersonal = numeroPersonal; }

        public String getCedulaProfesional() { return cedulaProfesional; }
        public void setCedulaProfesional(String cedulaProfesional) { this.cedulaProfesional = cedulaProfesional; }

        public String getContrasena() { return contrasena; }
        public void setContrasena(String contrasena) { this.contrasena = contrasena; }

        public byte[] getFotografia() { return fotografia; }
        public void setFotografia(byte[] fotografia) { this.fotografia = fotografia; }

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

        public boolean estaActivo() {
            return "Activo".equalsIgnoreCase(estatus);
        }

        @Override
        public String toString() {
            return "Dr(a). " + getNombreCompleto() + " - " + cedulaProfesional;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Medico medico = (Medico) obj;
            return idMedico == medico.idMedico;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(idMedico);
        }
}
