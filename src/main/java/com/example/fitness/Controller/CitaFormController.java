package com.example.fitness.Controller;

import com.example.fitness.DAO.CitaDAO;
import com.example.fitness.DAO.PacienteDAO;
import com.example.fitness.model.Cita;
import com.example.fitness.model.Medico;
import com.example.fitness.model.Paciente;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class CitaFormController {

    @FXML private ComboBox<Paciente> cbPaciente;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHora;
    @FXML private TextArea txtObservaciones;
    @FXML private Button btnGuardar;

    private Medico medicoLogueado;
    private CitaDAO citaDAO = new CitaDAO();
    private PacienteDAO pacienteDAO = new PacienteDAO();
    private Cita citaActual;

    public void setMedico(Medico medico) {
        this.medicoLogueado = medico;
        if (medico != null) {
            System.out.println("[CitaFormController] setMedico: OK. Recibido médico con ID = " + medico.getIdMedico());
            cargarPacientesDelMedico();
        } else {
            System.out.println("[CitaFormController] setMedico: ALERTA. Se recibió un médico NULO.");
        }
    }

    public void setCita(Cita cita) {
        this.citaActual = cita;
        if (cita != null) {
            dpFecha.setValue(cita.getFechaCita());
            txtHora.setText(cita.getHoraCita().toString());
            txtObservaciones.setText(cita.getObservaciones());
            for (Paciente p : cbPaciente.getItems()) {
                if (p.getIdPaciente() == cita.getIdPaciente()) {
                    cbPaciente.setValue(p);
                    break;
                }
            }
        }
    }

    // ... (El método initialize se queda igual)
    @FXML
    public void initialize() {
        cbPaciente.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Paciente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombre() + " " + item.getApellidos());
                }
            }
        });
        cbPaciente.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Paciente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNombre() + " " + item.getApellidos());
                }
            }
        });

        final Callback<DatePicker, DateCell> dayCellFactory =
                new Callback<DatePicker, DateCell>() {
                    @Override
                    public DateCell call(final DatePicker datePicker) {
                        return new DateCell() {
                            @Override
                            public void updateItem(LocalDate item, boolean empty) {
                                super.updateItem(item, empty);

                                // Deshabilita fechas anteriores a hoy.
                                if (item.isBefore(LocalDate.now())) {
                                    setDisable(true);
                                    setStyle("-fx-background-color: #ffc0cb;"); // Color rosa pálido para indicar que está deshabilitado
                                }
                            }
                        };
                    }
                };
        dpFecha.setDayCellFactory(dayCellFactory);
        // Por defecto, al abrir el formulario
        dpFecha.setValue(LocalDate.now());
    }



    private void cargarPacientesDelMedico() {
        System.out.println("[CitaFormController] Iniciando carga de pacientes...");
        if (medicoLogueado != null) {
            System.out.println("    -> Llamando a pacienteDAO.getPacientesByMedico con ID = " + medicoLogueado.getIdMedico());
            List<Paciente> pacientes = pacienteDAO.getPacientesByMedico(medicoLogueado.getIdMedico());
            System.out.println("    -> DAO devolvió " + pacientes.size() + " pacientes.");
            cbPaciente.getItems().setAll(pacientes);
            if (pacientes.isEmpty()) {
                System.out.println("    -> ALERTA: La lista de pacientes está vacía. El ComboBox no mostrará nada.");
            }
        } else {
            System.out.println("    -> ERROR: 'medicoLogueado' es NULO. No se pueden cargar pacientes.");
        }
    }

    // ... (El resto de tus métodos handleGuardar, cerrarVentana, etc. se quedan igual)
    @FXML
    private void handleGuardar() {
        if (cbPaciente.getValue() == null || dpFecha.getValue() == null || txtHora.getText().trim().isEmpty()) {
            mostrarAlerta("Campos Incompletos", "Por favor, seleccione un paciente, fecha y hora.");
            return;
        }
        try {
            LocalTime hora = LocalTime.parse(txtHora.getText());
            if (citaActual == null) {
                Cita nuevaCita = new Cita();
                nuevaCita.setIdPaciente(cbPaciente.getValue().getIdPaciente());
                nuevaCita.setFechaCita(dpFecha.getValue());
                nuevaCita.setHoraCita(hora);
                nuevaCita.setObservaciones(txtObservaciones.getText());
                nuevaCita.setEstatus("Programada");
                if (citaDAO.create(nuevaCita)) {
                    cerrarVentana();
                } else {
                    mostrarAlerta("Error", "No se pudo guardar la nueva cita.");
                }
            } else {
                citaActual.setIdPaciente(cbPaciente.getValue().getIdPaciente());
                citaActual.setFechaCita(dpFecha.getValue());
                citaActual.setHoraCita(hora);
                citaActual.setObservaciones(txtObservaciones.getText());
                if (citaDAO.update(citaActual)) {
                    cerrarVentana();
                } else {
                    mostrarAlerta("Error", "No se pudo actualizar la cita.");
                }
            }
        } catch (DateTimeParseException e) {
            mostrarAlerta("Formato de Hora Inválido", "Por favor, ingrese la hora en formato HH:mm (ej. 14:30).");
        }
    }
    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }
    private void cerrarVentana() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
