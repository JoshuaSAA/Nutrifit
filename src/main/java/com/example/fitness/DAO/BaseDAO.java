package com.example.fitness.DAO; // O el paquete donde lo tengas

import java.sql.SQLException; // <-- No olvides este import
import java.util.List;

public interface BaseDAO<T> {
    boolean create(T t) throws SQLException; // <-- AÑADIR AQUÍ
    T read(int id) throws SQLException; // <-- AÑADIR AQUÍ
    List<T> readAll() throws SQLException; // <-- AÑADIR AQUÍ
    boolean update(T t) throws SQLException; // <-- AÑADIR AQUÍ
    boolean delete(int id) throws SQLException; // <-- AÑADIR AQUÍ
    List<T> search(String criteria) throws SQLException; // <-- AÑADIR AQUÍ
}