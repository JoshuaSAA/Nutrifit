package com.example.fitness.DAO;

import com.example.fitness.model.Paciente;
import database.DatabaseConnection;
import com.example.fitness.model.Medico;
import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO implements BaseDAO<Paciente> {
    private Connection connection;
    private MedicoDAO medicoDAO;

    public PacienteDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.medicoDAO = new MedicoDAO();
    }

    @Override
    public boolean create(Paciente paciente) {
        String sql = "INSERT INTO pacientes (nombre, apellidos, fecha_nacimiento, genero, peso, " +
                "estatura, talla, email, telefono, domicilio, fotografia, id_medico, estatus) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, paciente.getNombre());
            stmt.setString(2, paciente.getApellidos());
            stmt.setDate(3, Date.valueOf(paciente.getFechaNacimiento()));
            stmt.setString(4, paciente.getGenero());
            stmt.setBigDecimal(5, paciente.getPeso());
            stmt.setBigDecimal(6, paciente.getEstatura());
            stmt.setString(7, paciente.getTalla());
            stmt.setString(8, paciente.getEmail());
            stmt.setString(9, paciente.getTelefono());
            stmt.setString(10, paciente.getDomicilio());
            stmt.setBytes(11, paciente.getFotografia());
            stmt.setInt(12, paciente.getIdMedico());
            stmt.setString(13, paciente.getEstatus());

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    paciente.setIdPaciente(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al crear paciente: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Paciente read(int id) {
        String sql = "SELECT * FROM pacientes WHERE id_paciente = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPaciente(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al leer paciente: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Paciente> readAll() {
        String sql = "SELECT * FROM pacientes ORDER BY apellidos, nombre";
        List<Paciente> pacientes = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                pacientes.add(mapResultSetToPaciente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al leer todos los pacientes: " + e.getMessage());
        }
        return pacientes;
    }

    @Override
    public boolean update(Paciente paciente) {
        String sql = "UPDATE pacientes SET nombre = ?, apellidos = ?, fecha_nacimiento = ?, " +
                "genero = ?, peso = ?, estatura = ?, talla = ?, email = ?, telefono = ?, " +
                "domicilio = ?, fotografia = ?, id_medico = ?, estatus = ? WHERE id_paciente = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, paciente.getNombre());
            stmt.setString(2, paciente.getApellidos());
            stmt.setDate(3, Date.valueOf(paciente.getFechaNacimiento()));
            stmt.setString(4, paciente.getGenero());
            stmt.setBigDecimal(5, paciente.getPeso());
            stmt.setBigDecimal(6, paciente.getEstatura());
            stmt.setString(7, paciente.getTalla());
            stmt.setString(8, paciente.getEmail());
            stmt.setString(9, paciente.getTelefono());
            stmt.setString(10, paciente.getDomicilio());
            stmt.setBytes(11, paciente.getFotografia());
            stmt.setInt(12, paciente.getIdMedico());
            stmt.setString(13, paciente.getEstatus());
            stmt.setInt(14, paciente.getIdPaciente());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar paciente: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE pacientes SET estatus = 'Inactivo' WHERE id_paciente = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar paciente: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<Paciente> search(String criteria) {
        String sql = "SELECT * FROM pacientes WHERE " +
                "(nombre LIKE ? OR apellidos LIKE ? OR email LIKE ? OR telefono LIKE ?) " +
                "AND estatus = 'Activo' ORDER BY apellidos, nombre";
        List<Paciente> pacientes = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + criteria + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pacientes.add(mapResultSetToPaciente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar pacientes: " + e.getMessage());
        }
        return pacientes;
    }

    public List<Paciente> getPacientesByMedico(int idMedico) {
        String sql = "SELECT * FROM pacientes WHERE id_medico = ? AND estatus = 'Activo' ORDER BY apellidos, nombre";
        List<Paciente> pacientes = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idMedico);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                pacientes.add(mapResultSetToPaciente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener pacientes por médico: " + e.getMessage());
        }
        return pacientes;
    }

    public Paciente getPacienteByEmail(String email) {
        String sql = "SELECT * FROM pacientes WHERE email = ? AND estatus = 'Activo'";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPaciente(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar paciente por email: " + e.getMessage());
        }
        return null;
    }

    private Paciente mapResultSetToPaciente(ResultSet rs) throws SQLException {
        Paciente paciente = new Paciente();
        paciente.setIdPaciente(rs.getInt("id_paciente"));
        paciente.setNombre(rs.getString("nombre"));
        paciente.setApellidos(rs.getString("apellidos"));
        paciente.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
        paciente.setGenero(rs.getString("genero"));
        paciente.setPeso(rs.getBigDecimal("peso"));
        paciente.setEstatura(rs.getBigDecimal("estatura"));
        paciente.setTalla(rs.getString("talla"));
        paciente.setEmail(rs.getString("email"));
        paciente.setTelefono(rs.getString("telefono"));
        paciente.setDomicilio(rs.getString("domicilio"));
        paciente.setFotografia(rs.getBytes("fotografia"));
        paciente.setIdMedico(rs.getInt("id_medico"));
        paciente.setEstatus(rs.getString("estatus"));

        // Cargar información del médico
        Medico medico = medicoDAO.read(paciente.getIdMedico());
        paciente.setMedico(medico);

        Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            paciente.setFechaRegistro(fechaRegistro.toLocalDateTime());
        }

        Timestamp fechaModificacion = rs.getTimestamp("fecha_modificacion");
        if (fechaModificacion != null) {
            paciente.setFechaModificacion(fechaModificacion.toLocalDateTime());
        }

        return paciente;
    }


}
