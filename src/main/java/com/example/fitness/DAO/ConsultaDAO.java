package com.example.fitness.DAO;


import com.example.fitness.model.Consulta;
import com.example.fitness.model.Paciente;
import com.example.fitness.model.Dieta;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ConsultaDAO {

    private final Connection conn;

    public ConsultaDAO(Connection conn) {
        this.conn = conn;
    }

    public void insertar(Consulta consulta) throws SQLException {
        String sql = "INSERT INTO consultas (id_paciente, fecha_consulta, hora_consulta, peso_actual, talla_actual, imc, " +
                "id_dieta, observaciones, estatus, fecha_registro, fecha_modificacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, consulta.getIdPaciente());
            stmt.setDate(2, Date.valueOf(consulta.getFechaConsulta()));
            stmt.setTime(3, Time.valueOf(consulta.getHoraConsulta()));
            stmt.setBigDecimal(4, consulta.getPesoActual());
            stmt.setString(5, consulta.getTallaActual());
            stmt.setBigDecimal(6, consulta.getImc());
            stmt.setInt(7, consulta.getIdDieta());
            stmt.setString(8, consulta.getObservaciones());
            stmt.setString(9, consulta.getEstatus());
            stmt.setTimestamp(10, Timestamp.valueOf(consulta.getFechaRegistro()));
            stmt.setTimestamp(11, Timestamp.valueOf(consulta.getFechaModificacion()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    consulta.setIdConsulta(rs.getInt(1));
                }
            }
        }
    }

    public void actualizar(Consulta consulta) throws SQLException {
        String sql = "UPDATE consultas SET id_paciente = ?, fecha_consulta = ?, hora_consulta = ?, peso_actual = ?, talla_actual = ?, imc = ?, " +
                "id_dieta = ?, observaciones = ?, estatus = ?, fecha_modificacion = ? WHERE id_consulta = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, consulta.getIdPaciente());
            stmt.setDate(2, Date.valueOf(consulta.getFechaConsulta()));
            stmt.setTime(3, Time.valueOf(consulta.getHoraConsulta()));
            stmt.setBigDecimal(4, consulta.getPesoActual());
            stmt.setString(5, consulta.getTallaActual());
            stmt.setBigDecimal(6, consulta.getImc());
            stmt.setInt(7, consulta.getIdDieta());
            stmt.setString(8, consulta.getObservaciones());
            stmt.setString(9, consulta.getEstatus());
            stmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(11, consulta.getIdConsulta());
            stmt.executeUpdate();
        }
    }

    public void eliminar(int idConsulta) throws SQLException {
        String sql = "DELETE FROM consultas WHERE id_consulta = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idConsulta);
            stmt.executeUpdate();
        }
    }

    public Consulta obtenerPorId(int idConsulta) throws SQLException {
        String sql = "SELECT * FROM consultas WHERE id_consulta = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idConsulta);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapConsulta(rs);
                }
            }
        }
        return null;
    }

    public List<Consulta> obtenerTodas() throws SQLException {
        List<Consulta> lista = new ArrayList<>();
        String sql = "SELECT * FROM consultas ORDER BY fecha_consulta DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapConsulta(rs));
            }
        }
        return lista;
    }

    private Consulta mapConsulta(ResultSet rs) throws SQLException {
        Consulta c = new Consulta();
        c.setIdConsulta(rs.getInt("id_consulta"));
        c.setIdPaciente(rs.getInt("id_paciente"));
        c.setFechaConsulta(rs.getDate("fecha_consulta").toLocalDate());
        c.setHoraConsulta(rs.getTime("hora_consulta").toLocalTime());
        c.setPesoActual(rs.getBigDecimal("peso_actual"));
        c.setTallaActual(rs.getString("talla_actual"));
        c.setImc(rs.getBigDecimal("imc"));
        c.setIdDieta(rs.getInt("id_dieta"));
        c.setObservaciones(rs.getString("observaciones"));
        c.setEstatus(rs.getString("estatus"));
        c.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        c.setFechaModificacion(rs.getTimestamp("fecha_modificacion").toLocalDateTime());

        // Puedes agregar carga de paciente y dieta aquí si tienes sus DAOs
        return c;
    }
}
