package com.example.fitness.DAO;

import com.example.fitness.model.HistorialMedidas;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HistorialMedidasDAO {
    private final Connection connection;

    public HistorialMedidasDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean insertar(HistorialMedidas historial) throws SQLException {
        String sql = "INSERT INTO historial_medidas (id_paciente, id_consulta, peso, estatura, talla, imc, fecha_medicion, observaciones) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, historial.getIdPaciente());
            stmt.setInt(2, historial.getIdConsulta());
            stmt.setBigDecimal(3, historial.getPeso());
            stmt.setBigDecimal(4, historial.getEstatura());
            stmt.setString(5, historial.getTalla());
            stmt.setBigDecimal(6, historial.getImc());
            stmt.setDate(7, Date.valueOf(historial.getFechaMedicion()));
            stmt.setString(8, historial.getObservaciones());

            int filasInsertadas = stmt.executeUpdate();

            if (filasInsertadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    historial.setIdHistorial(rs.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean actualizar(HistorialMedidas historial) throws SQLException {
        String sql = "UPDATE historial_medidas SET id_paciente = ?, id_consulta = ?, peso = ?, estatura = ?, talla = ?, imc = ?, fecha_medicion = ?, observaciones = ? " +
                "WHERE id_historial = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, historial.getIdPaciente());
            stmt.setInt(2, historial.getIdConsulta());
            stmt.setBigDecimal(3, historial.getPeso());
            stmt.setBigDecimal(4, historial.getEstatura());
            stmt.setString(5, historial.getTalla());
            stmt.setBigDecimal(6, historial.getImc());
            stmt.setDate(7, Date.valueOf(historial.getFechaMedicion()));
            stmt.setString(8, historial.getObservaciones());
            stmt.setInt(9, historial.getIdHistorial());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int idHistorial) throws SQLException {
        String sql = "DELETE FROM historial_medidas WHERE id_historial = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idHistorial);
            return stmt.executeUpdate() > 0;
        }
    }

    public HistorialMedidas obtenerPorId(int idHistorial) throws SQLException {
        String sql = "SELECT * FROM historial_medidas WHERE id_historial = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idHistorial);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapearDesdeResultSet(rs);
            }
        }
        return null;
    }

    public List<HistorialMedidas> obtenerPorPaciente(int idPaciente) throws SQLException {
        List<HistorialMedidas> lista = new ArrayList<>();
        String sql = "SELECT * FROM historial_medidas WHERE id_paciente = ? ORDER BY fecha_medicion DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPaciente);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(mapearDesdeResultSet(rs));
            }
        }
        return lista;
    }

    private HistorialMedidas mapearDesdeResultSet(ResultSet rs) throws SQLException {
        HistorialMedidas h = new HistorialMedidas();
        h.setIdHistorial(rs.getInt("id_historial"));
        h.setIdPaciente(rs.getInt("id_paciente"));
        h.setIdConsulta(rs.getInt("id_consulta"));
        h.setPeso(rs.getBigDecimal("peso"));
        h.setEstatura(rs.getBigDecimal("estatura"));
        h.setTalla(rs.getString("talla"));
        h.setImc(rs.getBigDecimal("imc"));
        h.setFechaMedicion(rs.getDate("fecha_medicion").toLocalDate());
        h.setObservaciones(rs.getString("observaciones"));
        return h;
    }
}
