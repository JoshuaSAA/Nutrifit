package com.example.fitness.DAO;

import com.example.fitness.model.Medico;
import database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MedicoDAO implements BaseDAO<Medico> {
    private Connection connection;

    public MedicoDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public boolean create(Medico medico) {
        String sql = "INSERT INTO medicos (nombre, apellidos, fecha_nacimiento, genero, domicilio, " +
                "numero_personal, cedula_profesional, contrasena, fotografia, estatus) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, SHA2(?, 256), ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, medico.getNombre());
            stmt.setString(2, medico.getApellidos());
            stmt.setDate(3, Date.valueOf(medico.getFechaNacimiento()));
            stmt.setString(4, medico.getGenero());
            stmt.setString(5, medico.getDomicilio());
            stmt.setString(6, medico.getNumeroPersonal());
            stmt.setString(7, medico.getCedulaProfesional());
            stmt.setString(8, medico.getContrasena());
            stmt.setBytes(9, medico.getFotografia());
            stmt.setString(10, medico.getEstatus());

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    medico.setIdMedico(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al crear médico: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Medico read(int id) {
        String sql = "SELECT * FROM medicos WHERE id_medico = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToMedico(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al leer médico: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Medico> readAll() {
        String sql = "SELECT * FROM medicos ORDER BY apellidos, nombre";
        List<Medico> medicos = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                medicos.add(mapResultSetToMedico(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al leer todos los médicos: " + e.getMessage());
        }
        return medicos;
    }

    @Override
    public boolean update(Medico medico) {
        String sql = "UPDATE medicos SET nombre = ?, apellidos = ?, fecha_nacimiento = ?, " +
                "genero = ?, domicilio = ?, numero_personal = ?, cedula_profesional = ?, " +
                "fotografia = ?, estatus = ? WHERE id_medico = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, medico.getNombre());
            stmt.setString(2, medico.getApellidos());
            stmt.setDate(3, Date.valueOf(medico.getFechaNacimiento()));
            stmt.setString(4, medico.getGenero());
            stmt.setString(5, medico.getDomicilio());
            stmt.setString(6, medico.getNumeroPersonal());
            stmt.setString(7, medico.getCedulaProfesional());
            stmt.setBytes(8, medico.getFotografia());
            stmt.setString(9, medico.getEstatus());
            stmt.setInt(10, medico.getIdMedico());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar médico: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE medicos SET estatus = 'Inactivo' WHERE id_medico = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar médico: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<Medico> search(String criteria) {
        String sql = "SELECT * FROM medicos WHERE " +
                "(nombre LIKE ? OR apellidos LIKE ? OR cedula_profesional LIKE ?) " +
                "AND estatus = 'Activo' ORDER BY apellidos, nombre";
        List<Medico> medicos = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + criteria + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                medicos.add(mapResultSetToMedico(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar médicos: " + e.getMessage());
        }
        return medicos;
    }

    public Medico authenticate(String numeroPersonal, String contrasena) {
        String sql = "SELECT * FROM medicos WHERE numero_personal = ? AND contrasena = SHA2(?, 256) AND estatus = 'Activo'";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, numeroPersonal);
            stmt.setString(2, contrasena);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToMedico(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error en autenticación: " + e.getMessage());
        }
        return null;
    }

    public List<Medico> getMedicosActivos() {
        String sql = "SELECT * FROM medicos WHERE estatus = 'Activo' ORDER BY apellidos, nombre";
        List<Medico> medicos = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                medicos.add(mapResultSetToMedico(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener médicos activos: " + e.getMessage());
        }
        return medicos;
    }

    private Medico mapResultSetToMedico(ResultSet rs) throws SQLException {
        Medico medico = new Medico();
        medico.setIdMedico(rs.getInt("id_medico"));
        medico.setNombre(rs.getString("nombre"));
        medico.setApellidos(rs.getString("apellidos"));
        medico.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
        medico.setGenero(rs.getString("genero"));
        medico.setDomicilio(rs.getString("domicilio"));
        medico.setNumeroPersonal(rs.getString("numero_personal"));
        medico.setCedulaProfesional(rs.getString("cedula_profesional"));
        medico.setFotografia(rs.getBytes("fotografia"));
        medico.setEstatus(rs.getString("estatus"));

        Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            medico.setFechaRegistro(fechaRegistro.toLocalDateTime());
        }

        Timestamp fechaModificacion = rs.getTimestamp("fecha_modificacion");
        if (fechaModificacion != null) {
            medico.setFechaModificacion(fechaModificacion.toLocalDateTime());
        }

        return medico;
    }
}
