package com.example.fitness.Controller;

import com.example.fitness.DAO.HistorialMedidasDAO;
import com.example.fitness.model.HistorialMedidas;
import com.example.fitness.model.Paciente;
import database.DatabaseConnection;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class HistorialPacienteController {

    @FXML private Label lblNombrePaciente;
    @FXML private TableView<HistorialMedidas> tablaMedidas;
    @FXML private TableColumn<HistorialMedidas, LocalDate> colFecha;
    @FXML private TableColumn<HistorialMedidas, Number> colPeso;
    @FXML private TableColumn<HistorialMedidas, Number> colEstatura;
    @FXML private TableColumn<HistorialMedidas, Number> colIMC;
    @FXML private TableColumn<HistorialMedidas, String> colTalla;
    @FXML private TableColumn<HistorialMedidas, String> colObservaciones;
    @FXML private Button btnNuevaConsulta; // <-- AÑADIDO


    private Paciente pacienteActual;
    private HistorialMedidasDAO historialMedidasDAO;
    private ObservableList<HistorialMedidas> listaMedidas = FXCollections.observableArrayList();

    public HistorialPacienteController() {
        // Inicializamos el DAO como en tu código
        this.historialMedidasDAO = new HistorialMedidasDAO(DatabaseConnection.getInstance().getConnection());
    }

    public void setPaciente(Paciente paciente) {
        this.pacienteActual = paciente;
        lblNombrePaciente.setText("Historial de Medidas de: " + paciente.getNombreCompleto());

        if (!"Activo".equalsIgnoreCase(paciente.getEstatus())) {
            btnNuevaConsulta.setDisable(true);
            btnNuevaConsulta.setText("No se pueden registrar consultas (Paciente Inactivo)");
        }

        cargarHistorial();
    }

    @FXML
    public void initialize() {
        colFecha.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getFechaMedicion()));
        colPeso.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPeso()));
        colEstatura.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getEstatura()));
        colIMC.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getImc()));
        colTalla.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTalla()));
        colObservaciones.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getObservaciones()));

        tablaMedidas.setItems(listaMedidas);
    }

    private void cargarHistorial() {
        if (pacienteActual != null) {
            try {
                listaMedidas.setAll(historialMedidasDAO.obtenerPorPaciente(pacienteActual.getIdPaciente()));
            } catch (SQLException e) {
                e.printStackTrace();
                // Opcional: mostrar alerta de error.
            }
        }
    }

    @FXML
    private void handleNuevaConsulta() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fitness/consulta-form-view.fxml"));
            Parent root = loader.load();

            ConsultaFormController controller = loader.getController();
            controller.setPaciente(pacienteActual);

            Stage stage = new Stage();
            stage.setTitle("Registrar Nueva Consulta para " + pacienteActual.getNombre());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refrescar la tabla al cerrar el formulario
            cargarHistorial();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
