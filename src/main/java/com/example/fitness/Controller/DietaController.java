package com.example.fitness.Controller;

import com.example.fitness.DAO.DietaDAO;
import com.example.fitness.model.Dieta;
import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.Optional;

public class DietaController {

    @FXML private TableView<Dieta> tablaDietas;
    @FXML private TableColumn<Dieta, String> colNombre;
    @FXML private TableColumn<Dieta, Integer> colCalorias;
    @FXML private TableColumn<Dieta, String> colDescripcion;

    @FXML private TextField txtNombre;
    @FXML private TextField txtCalorias;
    @FXML private TextArea txtDescripcion;

    private DietaDAO dietaDAO;
    private ObservableList<Dieta> dietasList;
    private Dieta dietaSeleccionada;

    @FXML
    public void initialize() {
        dietaDAO = new DietaDAO(DatabaseConnection.getInstance().getConnection());
        dietasList = FXCollections.observableArrayList();

        configurarTabla();
        cargarDietas();

        tablaDietas.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> mostrarDetallesDieta(newVal));
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreDieta"));
        colCalorias.setCellValueFactory(new PropertyValueFactory<>("caloriasTotales"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        tablaDietas.setItems(dietasList);
    }

    private void cargarDietas() {
        try {
            dietasList.setAll(dietaDAO.listarTodas());
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Carga", "No se pudieron cargar las dietas.", Alert.AlertType.ERROR);
        }
    }

    private void mostrarDetallesDieta(Dieta dieta) {
        dietaSeleccionada = dieta;
        if (dieta != null) {
            txtNombre.setText(dieta.getNombreDieta());
            txtCalorias.setText(String.valueOf(dieta.getCaloriasTotales()));
            txtDescripcion.setText(dieta.getDescripcion());
        } else {
            limpiarFormulario();
        }
    }

    @FXML
    private void handleNuevo() {
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        dietaSeleccionada = null;
        txtNombre.clear();
        txtCalorias.clear();
        txtDescripcion.clear();
        tablaDietas.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleGuardar() {
        if (txtNombre.getText().isEmpty() || txtCalorias.getText().isEmpty()) {
            mostrarAlerta("Campos Vacíos", "El nombre y las calorías son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            boolean esNuevo = (dietaSeleccionada == null);
            Dieta dieta = esNuevo ? new Dieta() : dietaSeleccionada;

            dieta.setNombreDieta(txtNombre.getText());
            dieta.setCaloriasTotales(Integer.parseInt(txtCalorias.getText()));
            dieta.setDescripcion(txtDescripcion.getText());

            if (esNuevo) {
                dietaDAO.insertar(dieta);
            } else {
                dietaDAO.actualizar(dieta);
            }

            mostrarAlerta("Éxito", "Dieta guardada correctamente.", Alert.AlertType.INFORMATION);
            cargarDietas();
            limpiarFormulario();

        } catch (NumberFormatException e) {
            mostrarAlerta("Dato Inválido", "Las calorías deben ser un número entero.", Alert.AlertType.WARNING);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Base de Datos", "No se pudo guardar la dieta.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEliminar() {
        if (dietaSeleccionada == null) {
            mostrarAlerta("Sin Selección", "Por favor, seleccione una dieta para eliminar.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de que desea eliminar la dieta '" + dietaSeleccionada.getNombreDieta() + "'?", ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText(null);

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                if (dietaDAO.eliminar(dietaSeleccionada.getIdDieta())) {
                    mostrarAlerta("Éxito", "La dieta ha sido eliminada.", Alert.AlertType.INFORMATION);
                    cargarDietas();
                    limpiarFormulario();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar la dieta.", Alert.AlertType.ERROR);
                }
            } catch (SQLException e) {
                // Esto puede pasar si la dieta está asignada a una consulta (foreign key constraint)
                e.printStackTrace();
                mostrarAlerta("Error de Base de Datos", "No se puede eliminar la dieta porque está en uso en una o más consultas.", Alert.AlertType.ERROR);
            }
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
