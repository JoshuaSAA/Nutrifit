package com.example.fitness.DAO;

import com.example.fitness.model.Dieta;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DietaDAO {
    private final Connection conn;

    public DietaDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean insertar(Dieta dieta) throws SQLException {
        String sql = "INSERT INTO dietas (nombre_dieta, descripcion, calorias_totales, fecha_creacion) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, dieta.getNombreDieta());
            stmt.setString(2, dieta.getDescripcion());
            stmt.setInt(3, dieta.getCaloriasTotales());
            stmt.setTimestamp(4, Timestamp.valueOf(dieta.getFechaCreacion()));

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        dieta.setIdDieta(rs.getInt(1));
                    }
                }
            }
            return filas > 0;
        }
    }

    public boolean actualizar(Dieta dieta) throws SQLException {
        String sql = "UPDATE dietas SET nombre_dieta = ?, descripcion = ?, calorias_totales = ? WHERE id_dieta = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dieta.getNombreDieta());
            stmt.setString(2, dieta.getDescripcion());
            stmt.setInt(3, dieta.getCaloriasTotales());
            stmt.setInt(4, dieta.getIdDieta());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int idDieta) throws SQLException {
        String sql = "DELETE FROM dietas WHERE id_dieta = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idDieta);
            return stmt.executeUpdate() > 0;
        }
    }

    public Dieta buscarPorId(int idDieta) throws SQLException {
        String sql = "SELECT * FROM dietas WHERE id_dieta = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idDieta);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearDieta(rs);
                }
            }
        }
        return null;
    }

    public List<Dieta> listarTodas() throws SQLException {
        List<Dieta> lista = new ArrayList<>();
        String sql = "SELECT * FROM dietas ORDER BY fecha_creacion DESC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearDieta(rs));
            }
        }
        return lista;
    }

    private Dieta mapearDieta(ResultSet rs) throws SQLException {
        Dieta dieta = new Dieta();
        dieta.setIdDieta(rs.getInt("id_dieta"));
        dieta.setNombreDieta(rs.getString("nombre_dieta"));
        dieta.setDescripcion(rs.getString("descripcion"));
        dieta.setCaloriasTotales(rs.getInt("calorias_totales"));
        Timestamp fecha = rs.getTimestamp("fecha_creacion");
        dieta.setFechaCreacion(fecha != null ? fecha.toLocalDateTime() : LocalDateTime.now());
        return dieta;
    }
}
