package com.example.fitness.Controller;

import com.example.fitness.DAO.ConsultaDAO;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConsultaFormController {

    // --- Controles FXML ---
    @FXML private Label lblTitulo;
    @FXML private TextField txtPeso;
    @FXML private ComboBox<Integer> cbMetros;
    @FXML private ComboBox<Integer> cbCentimetros;
    @FXML private TextField txtTallaRopa;
    @FXML private Label lblIMC;
    @FXML private TextArea txtObservacionesConsulta;
    @FXML private TextArea txtObservacionesMedidas;
    @FXML private TextField txtDietaSeleccionada;
    @FXML private Button btnGuardar;
    @FXML private Button btnSeleccionarDieta;

    // --- Clases y Datos ---
    private Paciente pacienteActual;
    private ConsultaDAO consultaDAO;
    private HistorialMedidasDAO historialMedidasDAO;
    private Dieta dietaSeleccionada;

    public ConsultaFormController() {
        this.consultaDAO = new ConsultaDAO(DatabaseConnection.getInstance().getConnection());
        this.historialMedidasDAO = new HistorialMedidasDAO(DatabaseConnection.getInstance().getConnection());
    }

    public void setPaciente(Paciente paciente) {
        this.pacienteActual = paciente;
        lblTitulo.setText("Nueva Consulta para: " + paciente.getNombreCompleto());
    }

    @FXML
    public void initialize() {
        // --- VALIDACIÓN ROBUSTA PARA EL PESO ---
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getControlNewText();
            if (text.matches("([0-9]*[.])?[0-9]*")) {
                return change;
            }
            return null;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        txtPeso.setTextFormatter(textFormatter);

        // --- POBLAR COMBOBOXES DE ALTURA ---
        ObservableList<Integer> metros = FXCollections.observableArrayList(0, 1, 2);
        ObservableList<Integer> centimetros = FXCollections.observableArrayList(
                IntStream.range(0, 100).boxed().collect(Collectors.toList())
        );
        cbMetros.setItems(metros);
        cbCentimetros.setItems(centimetros);

        // --- LISTENERS PARA CALCULAR IMC ---
        txtPeso.textProperty().addListener((obs, oldVal, newVal) -> calcularIMC());
        cbMetros.valueProperty().addListener((obs, oldVal, newVal) -> calcularIMC());
        cbCentimetros.valueProperty().addListener((obs, oldVal, newVal) -> calcularIMC());
    }

    @FXML
    private void handleSeleccionarDieta() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fitness/seleccionar-dieta-view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Seleccionar una Dieta");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            SeleccionarDietaController controller = loader.getController();
            stage.showAndWait();
            Dieta dieta = controller.getDietaSeleccionada();
            if (dieta != null) {
                this.dietaSeleccionada = dieta;
                txtDietaSeleccionada.setText(dieta.getNombreDieta());
            }
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Carga", "No se pudo abrir el selector de dietas.", Alert.AlertType.ERROR);
        }
    }

    private void calcularIMC() {
        try {
            if (txtPeso.getText().isEmpty() || cbMetros.getValue() == null || cbCentimetros.getValue() == null) {
                lblIMC.setText("-");
                return;
            }
            BigDecimal peso = new BigDecimal(txtPeso.getText());
            Integer metrosVal = cbMetros.getValue();
            Integer centimetrosVal = cbCentimetros.getValue();
            String estaturaStr = metrosVal + "." + String.format("%02d", centimetrosVal);
            BigDecimal estatura = new BigDecimal(estaturaStr);

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
        if (dietaSeleccionada == null || txtPeso.getText().isEmpty() || cbMetros.getValue() == null || cbCentimetros.getValue() == null) {
            mostrarAlerta("Datos incompletos", "Peso, estatura y una dieta seleccionada son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            BigDecimal peso = new BigDecimal(txtPeso.getText());
            Integer metros = cbMetros.getValue();
            Integer centimetros = cbCentimetros.getValue();
            String estaturaStr = metros + "." + String.format("%02d", centimetros);
            BigDecimal estatura = new BigDecimal(estaturaStr);
            BigDecimal imc = new BigDecimal(lblIMC.getText());

            Consulta nuevaConsulta = new Consulta();
            nuevaConsulta.setIdPaciente(pacienteActual.getIdPaciente());
            nuevaConsulta.setFechaConsulta(LocalDate.now());
            nuevaConsulta.setHoraConsulta(LocalTime.now());
            nuevaConsulta.setObservaciones(txtObservacionesConsulta.getText());
            nuevaConsulta.setIdDieta(dietaSeleccionada.getIdDieta());
            nuevaConsulta.setEstatus("Finalizada");
            nuevaConsulta.setFechaRegistro(LocalDateTime.now());
            nuevaConsulta.setFechaModificacion(LocalDateTime.now());
            nuevaConsulta.setPesoActual(peso);
            nuevaConsulta.setTallaActual(txtTallaRopa.getText());
            nuevaConsulta.setImc(imc);

            consultaDAO.insertar(nuevaConsulta);

            HistorialMedidas nuevasMedidas = new HistorialMedidas();
            nuevasMedidas.setIdConsulta(nuevaConsulta.getIdConsulta());
            nuevasMedidas.setIdPaciente(pacienteActual.getIdPaciente());
            nuevasMedidas.setFechaMedicion(LocalDate.now());
            nuevasMedidas.setPeso(peso);
            nuevasMedidas.setEstatura(estatura);
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
            mostrarAlerta("Datos Inválidos", "Por favor, ingrese un valor numérico válido para el peso.", Alert.AlertType.WARNING);
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
