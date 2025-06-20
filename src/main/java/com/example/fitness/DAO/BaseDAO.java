package com.example.fitness.DAO;

import java.util.List;

public interface BaseDAO<T> {
    boolean create(T entity);
    T read(int id);
    List<T> readAll();
    boolean update(T entity);
    boolean delete(int id);
    List<T> search(String criteria);
}