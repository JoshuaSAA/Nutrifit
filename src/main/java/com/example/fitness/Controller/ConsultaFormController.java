package com.example.fitness.Controller;

import com.example.fitness.DAO.ConsultaDAO;
import com.example.fitness.DAO.DietaDAO;
import com.example.fitness.DAO.HistorialMedidasDAO;
import com.example.fitness.model.Consulta;
import com.example.fitness.model.Dieta;
import com.example.fitness.model.HistorialMedidas;
import com.example.fitness.model.Paciente;
import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ConsultaFormController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtPeso;
    @FXML private TextField txtEstatura;
    @FXML private TextField txtTallaRopa;
    @FXML private Label lblIMC;
    @FXML private TextArea txtObservacionesConsulta;
    @FXML private TextArea txtObservacionesMedidas;
    @FXML private ComboBox<Dieta> cbDieta;
    @FXML private Button btnGuardar;

    private Paciente pacienteActual;
    private ConsultaDAO consultaDAO;
    private HistorialMedidasDAO historialMedidasDAO;
    private DietaDAO dietaDAO;

    public ConsultaFormController() {
        this.consultaDAO = new ConsultaDAO(DatabaseConnection.getInstance().getConnection());
        this.historialMedidasDAO = new HistorialMedidasDAO(DatabaseConnection.getInstance().getConnection());
        this.dietaDAO = new DietaDAO(DatabaseConnection.getInstance().getConnection());
    }

    public void setPaciente(Paciente paciente) {
        this.pacienteActual = paciente;
        lblTitulo.setText("Nueva Consulta para: " + paciente.getNombreCompleto());
    }

    @FXML
    public void initialize() {
        txtPeso.textProperty().addListener((obs, oldVal, newVal) -> calcularIMC());
        txtEstatura.textProperty().addListener((obs, oldVal, newVal) -> calcularIMC());
        cargarDietas();
    }

    private void cargarDietas() {
        try {
            List<Dieta> dietas = dietaDAO.listarTodas();
            cbDieta.getItems().setAll(dietas);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Carga", "No se pudieron cargar las dietas desde la base de datos.", Alert.AlertType.ERROR);
        }
    }

    private void calcularIMC() {
        try {
            BigDecimal peso = new BigDecimal(txtPeso.getText());
            BigDecimal estatura = new BigDecimal(txtEstatura.getText());
            if (estatura.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal imc = peso.divide(estatura.multiply(estatura), 2, RoundingMode.HALF_UP);
                lblIMC.setText(imc.toString());
            }
        } catch (Exception e) {
            lblIMC.setText("-");
        }
    }

    @FXML
    private void handleGuardar() {
        try {
            Dieta dietaSeleccionada = cbDieta.getValue();
            if (dietaSeleccionada == null) {
                mostrarAlerta("Datos incompletos", "Por favor, seleccione una dieta para el paciente.", Alert.AlertType.WARNING);
                return;
            }

            // 1. Crear y guardar la Consulta principal
            Consulta nuevaConsulta = new Consulta();
            nuevaConsulta.setIdPaciente(pacienteActual.getIdPaciente());
            nuevaConsulta.setFechaConsulta(LocalDate.now());
            nuevaConsulta.setHoraConsulta(LocalTime.now());
            nuevaConsulta.setObservaciones(txtObservacionesConsulta.getText());
            nuevaConsulta.setIdDieta(dietaSeleccionada.getIdDieta());


            nuevaConsulta.setEstatus("Finalizada");


            nuevaConsulta.setFechaRegistro(LocalDateTime.now());
            nuevaConsulta.setFechaModificacion(LocalDateTime.now());

            BigDecimal peso = new BigDecimal(txtPeso.getText());
            BigDecimal imc = new BigDecimal(lblIMC.getText());
            nuevaConsulta.setPesoActual(peso);
            nuevaConsulta.setTallaActual(txtTallaRopa.getText());
            nuevaConsulta.setImc(imc);

            consultaDAO.insertar(nuevaConsulta);

            // 2. Crear y guardar el Historial de Medidas
            HistorialMedidas nuevasMedidas = new HistorialMedidas();
            nuevasMedidas.setIdConsulta(nuevaConsulta.getIdConsulta());
            nuevasMedidas.setIdPaciente(pacienteActual.getIdPaciente());
            nuevasMedidas.setFechaMedicion(LocalDate.now());
            nuevasMedidas.setPeso(peso);
            nuevasMedidas.setEstatura(new BigDecimal(txtEstatura.getText()));
            nuevasMedidas.setTalla(txtTallaRopa.getText());
            nuevasMedidas.setImc(imc);
            nuevasMedidas.setObservaciones(txtObservacionesMedidas.getText());

            historialMedidasDAO.insertar(nuevasMedidas);

            mostrarAlerta("Éxito", "La consulta y las medidas se han guardado correctamente.", Alert.AlertType.INFORMATION);
            cerrarVentana();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Base de Datos", "No se pudo guardar la información. Razón: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (NumberFormatException e) {
            mostrarAlerta("Datos Inválidos", "Por favor, ingrese valores numéricos válidos para peso y estatura.", Alert.AlertType.WARNING);
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

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
