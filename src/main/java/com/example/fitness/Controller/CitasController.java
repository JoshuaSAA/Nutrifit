package com.example.fitness.Controller;

import com.example.fitness.DAO.CitaDAO;
import com.example.fitness.DAO.PacienteDAO;
import com.example.fitness.model.Cita;
import com.example.fitness.model.Medico;
import com.example.fitness.model.Paciente;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class CitasController {

    @FXML private TableView<Cita> tablaCitas;
    @FXML private TableColumn<Cita, String> colFecha;
    @FXML private TableColumn<Cita, String> colHora;
    @FXML private TableColumn<Cita, String> colPaciente;
    @FXML private TableColumn<Cita, String> colEstatus;
    @FXML private DatePicker datePickerBusqueda;
    @FXML private TextField txtBuscarPaciente;

    @FXML private Button btnAgendarCita;
    @FXML private Button btnModificarCita;
    @FXML private Button btnCancelarCita;

    private Medico medicoLogueado;
    private CitaDAO citaDAO = new CitaDAO();
    private ObservableList<Cita> listaCitas = FXCollections.observableArrayList();

    public void setMedico(Medico medico) {
        this.medicoLogueado = medico;
        if (medico != null) {
            System.out.println("[CitasController] setMedico: OK. Recibido médico con ID = " + medico.getIdMedico());
            datePickerBusqueda.setValue(LocalDate.now());
            cargarCitasPorFecha();
        } else {
            System.out.println("[CitasController] setMedico: ALERTA. Se recibió un médico NULO.");
        }
    }


    @FXML
    public void initialize() {
        colFecha.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFechaCita().toString()));
        colHora.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHoraCita().toString()));
        colPaciente.setCellValueFactory(cellData -> {
            Paciente paciente = cellData.getValue().getPaciente();
            if (paciente != null) {
                return new SimpleStringProperty(paciente.getNombre() + " " + paciente.getApellidos());
            }
            return new SimpleStringProperty("Paciente no encontrado");
        });
        configurarListeners();
        colEstatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstatus()));
        tablaCitas.setItems(listaCitas);
    }

    private void configurarListeners() {
        tablaCitas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean deshabilitar = true; // Por defecto, los botones de acción están deshabilitados
            if (newVal != null) {
                // Habilitamos los botones solo si la cita seleccionada NO está "Cancelada"
                if (!"Cancelada".equalsIgnoreCase(newVal.getEstatus())) {
                    deshabilitar = false;
                }
            }
            btnModificarCita.setDisable(deshabilitar);
            btnCancelarCita.setDisable(deshabilitar);
        });
    }
    @FXML
    private void cargarCitasPorFecha() {
        LocalDate fechaSeleccionada = datePickerBusqueda.getValue();
        if (medicoLogueado != null && fechaSeleccionada != null) {
            listaCitas.setAll(citaDAO.getCitasByMedicoAndFecha(medicoLogueado.getIdMedico(), fechaSeleccionada));
        }
    }
    @FXML
    private void buscarCitaPorPaciente() {
        String criterio = txtBuscarPaciente.getText();
        if (criterio != null && !criterio.trim().isEmpty()) {
            listaCitas.setAll(citaDAO.search(criterio));
        } else {
            cargarCitasPorFecha();
        }
    }
    @FXML
    private void limpiarBusqueda() {
        txtBuscarPaciente.clear();
        datePickerBusqueda.setValue(LocalDate.now());
        cargarCitasPorFecha();
    }
    @FXML
    private void handleModificarCita() {
        Cita seleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            abrirFormularioCita(seleccionada);
        } else {
            mostrarAlerta("Sin selección", "Por favor, seleccione una cita de la tabla para modificarla.");
        }
    }
    @FXML
    private void handleCancelarCita() {
        Cita seleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Cancelación");
            confirmacion.setHeaderText("¿Está seguro de que desea cancelar esta cita?");
            confirmacion.setContentText("Esta acción cambiará el estatus de la cita a 'Cancelada'.");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                if (citaDAO.delete(seleccionada.getIdCita())) {
                    mostrarAlerta("Éxito", "La cita ha sido cancelada correctamente.");
                    cargarCitasPorFecha();
                } else {
                    mostrarAlerta("Error", "No se pudo cancelar la cita.");
                }
            }
        } else {
            mostrarAlerta("Sin selección", "Por favor, seleccione una cita para cancelar.");
        }
    }
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    // ...

    @FXML
    private void handleAgendarCita() {
        abrirFormularioCita(null);
    }

    private void abrirFormularioCita(Cita cita) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fitness/cita-form-view.fxml"));
            Parent root = loader.load();

            System.out.println("[CitasController] Intentando pasar médico a CitaFormController...");
            if (this.medicoLogueado == null) {
                System.out.println("    -> [CitasController] ERROR: 'medicoLogueado' es NULO en este punto. No se puede pasar.");
            }

            CitaFormController controller = loader.getController();
            controller.setMedico(this.medicoLogueado);
            if (cita != null) {
                controller.setCita(cita);
            }

            Stage stage = new Stage();
            stage.setTitle(cita == null ? "Agendar Nueva Cita" : "Modificar Cita");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            cargarCitasPorFecha();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Carga", "No se pudo abrir el formulario de citas.");
        }
    }
    @FXML
    private void handleVerTodas() {
        // Limpiamos los filtros para que el usuario sepa que está viendo todo
        datePickerBusqueda.setValue(null);
        txtBuscarPaciente.clear();

        // Llamamos al método del DAO que lee todas las citas
        listaCitas.setAll(citaDAO.readAll());
    }
}
