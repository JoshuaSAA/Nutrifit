package com.example.fitness.Controller;

import com.example.fitness.DAO.ConsultaDAO;
import com.example.fitness.DAO.DietaDAO;
import com.example.fitness.DAO.HistorialMedidasDAO;
import com.example.fitness.model.Consulta;
import com.example.fitness.model.Dieta;
import com.example.fitness.model.HistorialMedidas;
import com.example.fitness.model.Paciente;
import database.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistorialCompletoController {

    // --- Controles FXML ---
    @FXML private Label lblNombrePaciente, lblSeleccion;
    @FXML private ListView<Consulta> listaConsultas;
    @FXML private VBox panelDetalles;
    @FXML private Label lblPeso, lblEstatura, lblTallaRopa, lblIMC;
    @FXML private Label lblNombreDieta, lblCalorias;
    @FXML private TextArea txtAreaDieta, txtAreaObservaciones,txtAreaObservacionesMedidas;
    @FXML private Button btnNuevaConsulta;

    // --- Clases y Datos ---
    private Paciente pacienteActual;
    private ConsultaDAO consultaDAO;
    private HistorialMedidasDAO historialMedidasDAO;
    private DietaDAO dietaDAO;
    private ObservableList<Consulta> consultasList = FXCollections.observableArrayList();

    public HistorialCompletoController() {
        // Inicializamos todos los DAOs que necesitamos
        this.consultaDAO = new ConsultaDAO(DatabaseConnection.getInstance().getConnection());
        this.historialMedidasDAO = new HistorialMedidasDAO(DatabaseConnection.getInstance().getConnection());
        this.dietaDAO = new DietaDAO(DatabaseConnection.getInstance().getConnection());
    }

    public void setPaciente(Paciente paciente) {
        this.pacienteActual = paciente;
        lblNombrePaciente.setText("Historial Completo de: " + paciente.getNombreCompleto());
        if (!"Activo".equalsIgnoreCase(paciente.getEstatus())) {
            btnNuevaConsulta.setDisable(true);
            btnNuevaConsulta.setText("No se pueden registrar consultas (Paciente Inactivo)");
        }
        cargarConsultasDelPaciente();
    }

    @FXML
    public void initialize() {
        // Formatear cómo se ven las consultas en la lista de la izquierda
        listaConsultas.setCellFactory(param -> new ListCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd / MMMM / yyyy");
            @Override
            protected void updateItem(Consulta item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFechaConsulta().format(formatter));
                }
            }
        });

        // Listener para cuando se selecciona una consulta de la lista
        listaConsultas.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> mostrarDetallesConsulta(newVal));

        listaConsultas.setItems(consultasList);
        panelDetalles.setVisible(false); // Ocultar panel de detalles al inicio
    }

    private void cargarConsultasDelPaciente() {
        if (pacienteActual != null) {
            try {
                List<Consulta> consultas = consultaDAO.obtenerPorPaciente(pacienteActual.getIdPaciente());
                consultasList.setAll(consultas);
                if(consultas.isEmpty()){
                    lblSeleccion.setText("Este paciente aún no tiene consultas registradas.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void mostrarDetallesConsulta(Consulta consulta) {
        if (consulta == null) {
            panelDetalles.setVisible(false);
            lblSeleccion.setVisible(true);
            return;
        }

        panelDetalles.setVisible(true);
        lblSeleccion.setVisible(false);

        try {
            // 1. Cargar Medidas
            HistorialMedidas medidas = historialMedidasDAO.obtenerPorIdConsulta(consulta.getIdConsulta());
            if (medidas != null) {
                lblPeso.setText(medidas.getPeso() + " kg");
                lblEstatura.setText(medidas.getEstatura() + " m");
                lblTallaRopa.setText(medidas.getTalla());
                lblIMC.setText(medidas.getImc().toString());
                txtAreaObservacionesMedidas.setText(medidas.getObservaciones());
            }

            // 2. Cargar Dieta
            Dieta dieta = dietaDAO.buscarPorId(consulta.getIdDieta());
            if (dieta != null) {
                lblNombreDieta.setText(dieta.getNombreDieta());
                lblCalorias.setText(dieta.getCaloriasTotales() + " kcal");
                txtAreaDieta.setText(dieta.getDescripcion());
            }

            // 3. Cargar Observaciones de la Consulta
            txtAreaObservaciones.setText(consulta.getObservaciones());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNuevaConsulta() {
        try {
            String fxmlPath = "/com/example/fitness/consulta-form-view.fxml";
            // Verificamos primero si el recurso existe
            URL resourceUrl = getClass().getResource(fxmlPath);

            if (resourceUrl == null) {
                // Si no existe, mostramos un error claro.
                System.err.println("Error Crítico: No se encontró el archivo FXML en la ruta: " + fxmlPath);
                Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo encontrar el archivo de la interfaz 'consulta-form-view.fxml'. Por favor, verifique la estructura del proyecto.");
                alert.showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();
            ConsultaFormController controller = loader.getController();
            controller.setPaciente(this.pacienteActual);

            Stage stage = new Stage();
            stage.setTitle("Registrar Nueva Consulta para " + pacienteActual.getNombre());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            cargarConsultasDelPaciente();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
