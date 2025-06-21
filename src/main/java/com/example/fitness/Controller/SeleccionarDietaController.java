package com.example.fitness.Controller;

import com.example.fitness.DAO.DietaDAO;
import com.example.fitness.model.Dieta;
import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class SeleccionarDietaController {

    @FXML private TextField txtBusquedaDieta;
    @FXML private TableView<Dieta> tablaDietas;
    @FXML private TableColumn<Dieta, String> colNombre;
    @FXML private TableColumn<Dieta, Integer> colCalorias;
    @FXML private TableColumn<Dieta, String> colDescripcion;
    @FXML private Button btnSeleccionar;

    private DietaDAO dietaDAO;
    private ObservableList<Dieta> dietasList = FXCollections.observableArrayList();
    private Dieta dietaSeleccionada;

    public SeleccionarDietaController() {
        this.dietaDAO = new DietaDAO(DatabaseConnection.getInstance().getConnection());
    }

    @FXML
    public void initialize() {
        configurarTabla();
        cargarDietas();
        configurarFiltro();
        btnSeleccionar.setDisable(true); // Deshabilitado hasta que se seleccione algo
        tablaDietas.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> btnSeleccionar.setDisable(newVal == null)
        );
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreDieta"));
        colCalorias.setCellValueFactory(new PropertyValueFactory<>("caloriasTotales"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
    }

    private void cargarDietas() {
        try {
            dietasList.setAll(dietaDAO.listarTodas());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void configurarFiltro() {
        FilteredList<Dieta> filteredData = new FilteredList<>(dietasList, p -> true);
        txtBusquedaDieta.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(dieta -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (dieta.getNombreDieta().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        tablaDietas.setItems(filteredData);
    }

    @FXML
    private void handleSeleccionar() {
        this.dietaSeleccionada = tablaDietas.getSelectionModel().getSelectedItem();
        cerrarVentana();
    }

    @FXML
    private void handleCrearNueva() {
        try {
            // Reutilizamos la vista de gestión de dietas
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fitness/dietas-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Crear Nueva Dieta");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Al cerrar, refrescamos la lista de dietas
            cargarDietas();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelar() {
        this.dietaSeleccionada = null; // Nos aseguramos de no devolver nada
        cerrarVentana();
    }

    private void cerrarVentana() {
        ((Stage) btnSeleccionar.getScene().getWindow()).close();
    }

    // Este método es la clave para devolver la dieta seleccionada
    public Dieta getDietaSeleccionada() {
        return dietaSeleccionada;
    }
}
