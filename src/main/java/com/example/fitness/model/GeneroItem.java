package com.example.fitness.model;

/**
 * Clase de ayuda para gestionar el género en los ComboBox.
 * Permite mostrar un texto completo al usuario (ej. "Masculino")
 * mientras se sigue guardando el código corto en la base de datos (ej. "M").
 */
public class GeneroItem {
    private final String codigo;
    private final String texto;

    public GeneroItem(String codigo, String texto) {
        this.codigo = codigo;
        this.texto = texto;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTexto() {
        return texto;
    }

    /**
     * Este método es la clave. El ComboBox lo usará automáticamente
     * para saber qué texto mostrar en la lista desplegable.
     */
    @Override
    public String toString() {
        return texto;
    }

    /**
     * Sobrescribimos equals para que Java pueda comparar objetos GeneroItem
     * basándose en su código, lo cual es útil para encontrar el item correcto.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GeneroItem that = (GeneroItem) obj;
        return codigo != null ? codigo.equals(that.codigo) : that.codigo == null;
    }

    @Override
    public int hashCode() {
        return codigo != null ? codigo.hashCode() : 0;
    }
}
