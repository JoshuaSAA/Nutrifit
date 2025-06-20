package com.example.fitness.DAO;



import database.DatabaseConnection;
import com.example.fitness.model.Cita;
import com.example.fitness.model.Paciente;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CitaDAO implements BaseDAO<Cita> {
    private Connection connection;
    private PacienteDAO pacienteDAO;

    public CitaDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.pacienteDAO = new PacienteDAO();
    }

    @Override
    public boolean create(Cita cita) {
        String sql = "INSERT INTO citas (fecha_cita, hora_cita, id_paciente, observaciones, estatus) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDate(1, Date.valueOf(cita.getFechaCita()));
            stmt.setTime(2, Time.valueOf(cita.getHoraCita()));
            stmt.setInt(3, cita.getIdPaciente());
            stmt.setString(4, cita.getObservaciones());
            stmt.setString(5, cita.getEstatus());

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    cita.setIdCita(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error al crear cita: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Cita read(int id) {
        String sql = "SELECT * FROM citas WHERE id_cita = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCita(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al leer cita: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Cita> readAll() {
        String sql = "SELECT * FROM citas ORDER BY fecha_cita DESC, hora_cita DESC";
        List<Cita> citas = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                citas.add(mapResultSetToCita(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al leer todas las citas: " + e.getMessage());
        }
        return citas;
    }

    @Override
    public boolean update(Cita cita) {
        String sql = "UPDATE citas SET fecha_cita = ?, hora_cita = ?, id_paciente = ?, " +
                "observaciones = ?, estatus = ? WHERE id_cita = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(cita.getFechaCita()));
            stmt.setTime(2, Time.valueOf(cita.getHoraCita()));
            stmt.setInt(3, cita.getIdPaciente());
            stmt.setString(4, cita.getObservaciones());
            stmt.setString(5, cita.getEstatus());
            stmt.setInt(6, cita.getIdCita());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar cita: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE citas SET estatus = 'Cancelada' WHERE id_cita = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al cancelar cita: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<Cita> search(String criteria) {
        String sql = "SELECT c.* FROM citas c " +
                "JOIN pacientes p ON c.id_paciente = p.id_paciente " +
                "WHERE (p.nombre LIKE ? OR p.apellidos LIKE ?) " +
                "ORDER BY c.fecha_cita DESC, c.hora_cita DESC";
        List<Cita> citas = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + criteria + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                citas.add(mapResultSetToCita(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar citas: " + e.getMessage());
        }
        return citas;
    }

    public List<Cita> getCitasByFecha(LocalDate fecha) {
        String sql = "SELECT * FROM citas WHERE fecha_cita = ? ORDER BY hora_cita";
        List<Cita> citas = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(fecha));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                citas.add(mapResultSetToCita(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener citas por fecha: " + e.getMessage());
        }
        return citas;
    }

    public List<Cita> getCitasByPaciente(int idPaciente) {
        String sql = "SELECT * FROM citas WHERE id_paciente = ? ORDER BY fecha_cita DESC, hora_cita DESC";
        List<Cita> citas = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPaciente);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                citas.add(mapResultSetToCita(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener citas por paciente: " + e.getMessage());
        }
        return citas;
    }

    public List<Cita> getCitasByMedicoAndFecha(int idMedico, LocalDate fecha) {
        String sql = "SELECT c.* FROM citas c " +
                "JOIN pacientes p ON c.id_paciente = p.id_paciente " +
                "WHERE p.id_medico = ? AND c.fecha_cita = ? " +
                "ORDER BY c.hora_cita";
        List<Cita> citas = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idMedico);
            stmt.setDate(2, Date.valueOf(fecha));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                citas.add(mapResultSetToCita(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener citas por médico y fecha: " + e.getMessage());
        }
        return citas;
    }

    private Cita mapResultSetToCita(ResultSet rs) throws SQLException {
        Cita cita = new Cita();
        cita.setIdCita(rs.getInt("id_cita"));
        cita.setFechaCita(rs.getDate("fecha_cita").toLocalDate());
        cita.setHoraCita(rs.getTime("hora_cita").toLocalTime());
        cita.setIdPaciente(rs.getInt("id_paciente"));
        cita.setObservaciones(rs.getString("observaciones"));
        cita.setEstatus(rs.getString("estatus"));

        // Cargar información del paciente
        Paciente paciente = pacienteDAO.read(cita.getIdPaciente());
        cita.setPaciente(paciente);

        Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            cita.setFechaRegistro(fechaRegistro.toLocalDateTime());
        }

        Timestamp fechaModificacion = rs.getTimestamp("fecha_modificacion");
        if (fechaModificacion != null) {
            cita.setFechaModificacion(fechaModificacion.toLocalDateTime());
        }

        return cita;
    }
}