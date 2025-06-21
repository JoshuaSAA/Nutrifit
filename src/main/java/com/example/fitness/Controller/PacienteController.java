package com.example.fitness.Controller;

import com.example.fitness.DAO.PacienteDAO;
import com.example.fitness.DAO.MedicoDAO;
import com.example.fitness.model.Paciente;
import com.example.fitness.model.Medico;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import com.example.fitness.model.GeneroItem;
import javafx.util.Callback;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PacienteController implements Initializable {

    // Controles de búsqueda y botones
    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;

    // Tabla de pacientes
    @FXML private TableView<Paciente> tablePacientes;
    @FXML private TableColumn<Paciente, Integer> colId;
    @FXML private TableColumn<Paciente, String> colNombre;
    @FXML private TableColumn<Paciente, String> colApellidos;
    @FXML private TableColumn<Paciente, String> colEmail;
    @FXML private TableColumn<Paciente, String> colTelefono;
    @FXML private TableColumn<Paciente, String> colGenero;
    @FXML private TableColumn<Paciente, BigDecimal> colPeso;
    @FXML private TableColumn<Paciente, BigDecimal> colEstatura;
    @FXML private TableColumn<Paciente, String> colMedico;
    @FXML private TableColumn<Paciente, String> colEstatus;

    // Panel de detalles
    @FXML private ImageView imgFotografia;
    @FXML private Button btnCambiarFoto;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private ComboBox<GeneroItem> cbGenero; // <-- CAMBIADO a GeneroItem
    @FXML private ComboBox<Integer> cbMetros; // <-- NUEVO
    @FXML private ComboBox<Integer> cbCentimetros;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtPeso;
    @FXML private TextField txtTalla;
    @FXML private TextArea txtDomicilio;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnVerHistorial;

    // DAO y datos
    private PacienteDAO pacienteDAO;
    private MedicoDAO medicoDAO;
    private Medico medicoLogueado;
    private ObservableList<Paciente> pacientesList;
    private Paciente pacienteSeleccionado;
    private boolean modoEdicion = false;
    private byte[] fotografiaBytes;

    private final ObservableList<GeneroItem> generos = FXCollections.observableArrayList(
            new GeneroItem("M", "Masculino"),
            new GeneroItem("F", "Femenino"),
            new GeneroItem("Otro", "Otro")
    );

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pacienteDAO = new PacienteDAO();
        medicoDAO = new MedicoDAO();
        pacientesList = FXCollections.observableArrayList();
        configurarTabla();
        configurarComboBoxes();
        final Callback<DatePicker, DateCell> dayCellFactory =
                datePicker -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);

                        // Deshabilita y colorea las fechas posteriores a hoy.
                        if (item.isAfter(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;"); // Estilo opcional para indicar que está deshabilitado
                        }
                    }
                };
        dpFechaNacimiento.setDayCellFactory(dayCellFactory);
        cargarPacientes();
        configurarListeners();
        deshabilitarEdicion();
    }

    public void setMedico(Medico medico) {
        this.medicoLogueado = medico;
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idPaciente"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        colPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));
        colEstatura.setCellValueFactory(new PropertyValueFactory<>("estatura"));
        colEstatus.setCellValueFactory(new PropertyValueFactory<>("estatus"));
        colMedico.setCellValueFactory(cellData -> {
            Medico medico = cellData.getValue().getMedico();
            if (medico != null) {
                return new SimpleStringProperty(medico.getNombre() + " " + medico.getApellidos());
            }
            return new SimpleStringProperty("");
        });
        tablePacientes.setItems(pacientesList);
    }

    private void configurarComboBoxes() {
        cbGenero.setItems(generos);
        ObservableList<Integer> metros = FXCollections.observableArrayList(0, 1, 2);
        ObservableList<Integer> centimetros = FXCollections.observableArrayList(
                IntStream.range(0, 100).boxed().collect(Collectors.toList())
        );
        cbMetros.setItems(metros);
        cbCentimetros.setItems(centimetros);
    }

    private void configurarListeners() {
        tablePacientes.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        pacienteSeleccionado = newValue;
                        mostrarDetallesPaciente(newValue);
                        boolean botonesDeshabilitados = true; // Por defecto, deshabilitados
                        if (newValue != null) {
                            // Solo se habilitan si el paciente está ACTIVO
                            if ("Activo".equalsIgnoreCase(newValue.getEstatus())) {
                                botonesDeshabilitados = false;
                            }
                        }

                        btnEditar.setDisable(botonesDeshabilitados);
                        btnEliminar.setDisable(botonesDeshabilitados);
                        btnVerHistorial.setDisable(false);
                    } else {
                        btnEditar.setDisable(true);
                        btnEliminar.setDisable(true);
                        btnVerHistorial.setDisable(true); // <-- AÑADIDO: Deshabilitar si no hay selección
                    }
                }
        );
    }

    private void cargarPacientes() {
        List<Paciente> pacientes = pacienteDAO.readAll();
        pacientesList.clear();
        pacientesList.addAll(pacientes);
    }

    private void mostrarDetallesPaciente(Paciente paciente) {
        txtNombre.setText(paciente.getNombre());
        txtApellidos.setText(paciente.getApellidos());
        dpFechaNacimiento.setValue(paciente.getFechaNacimiento());
        // GeneroItem
        Optional<GeneroItem> generoSeleccionado = generos.stream()
                .filter(item -> item.getCodigo().equals(paciente.getGenero()))
                .findFirst();
        generoSeleccionado.ifPresent(cbGenero::setValue);
        txtEmail.setText(paciente.getEmail());
        txtTelefono.setText(paciente.getTelefono());
        txtPeso.setText(paciente.getPeso() != null ? paciente.getPeso().toString() : "");
        if (paciente.getEstatura() != null) {
            BigDecimal estatura = paciente.getEstatura();
            int metros = estatura.intValue();
            int centimetros = estatura.subtract(new BigDecimal(metros)).multiply(new BigDecimal(100)).intValue();
            cbMetros.setValue(metros);
            cbCentimetros.setValue(centimetros);
        } else {
            cbMetros.setValue(null);
            cbCentimetros.setValue(null);
        };
        txtTalla.setText(paciente.getTalla());
        txtDomicilio.setText(paciente.getDomicilio());

        // Mostrar fotografía
        if (paciente.getFotografia() != null) {
            try {
                Image image = new Image(new ByteArrayInputStream(paciente.getFotografia()));
                imgFotografia.setImage(image);
            } catch (Exception e) {
                imgFotografia.setImage(null);
            }
        } else {
            imgFotografia.setImage(null);
        }

        fotografiaBytes = paciente.getFotografia();
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtApellidos.clear();
        dpFechaNacimiento.setValue(null);
        cbGenero.setValue(null);
        txtEmail.clear();
        txtTelefono.clear();
        txtPeso.clear();
        cbMetros.setValue(null);
        cbCentimetros.setValue(null);
        txtTalla.clear();
        txtDomicilio.clear();
        imgFotografia.setImage(null);
        fotografiaBytes = null;
    }

    // Métodos de acción de botones
    @FXML
    private void buscarPaciente() {
        String criterio = txtBuscar.getText().trim();
        if (!criterio.isEmpty()) {
            List<Paciente> pacientesEncontrados = pacienteDAO.search(criterio);
            pacientesList.clear();
            pacientesList.addAll(pacientesEncontrados);
        } else {
            cargarPacientes();
        }
    }

    @FXML
    private void limpiarBusqueda() {
        txtBuscar.clear();
        cargarPacientes();
    }

    @FXML
    private void nuevoPaciente() {
        modoEdicion = true;
        pacienteSeleccionado = null;
        limpiarFormulario();
        habilitarEdicion();
        tablePacientes.getSelectionModel().clearSelection();
    }

    @FXML
    private void editarPaciente() {
        if (pacienteSeleccionado != null) {
            modoEdicion = true;
            habilitarEdicion();
        } else {
            mostrarAlerta("Error", "Seleccione un paciente para editar", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void eliminarPaciente() {
        if (pacienteSeleccionado != null) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setHeaderText("¿Está seguro de dar de baja al paciente?");
            confirmacion.setContentText("Esta acción cambiará el estatus del paciente a 'Inactivo'");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                if (pacienteDAO.delete(pacienteSeleccionado.getIdPaciente())) {
                    mostrarAlerta("Éxito", "Paciente dado de baja correctamente", Alert.AlertType.INFORMATION);
                    cargarPacientes();
                    limpiarFormulario();
                    deshabilitarEdicion();
                } else {
                    mostrarAlerta("Error", "No se pudo dar de baja al paciente", Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Error", "Seleccione un paciente para eliminar", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void guardarPaciente() {
        if (!validarCampos()) {
            return;
        }

        boolean esNuevo = (pacienteSeleccionado == null);
        Paciente paciente = esNuevo ? new Paciente() : pacienteSeleccionado;

        // Asignar valores del formulario
        paciente.setNombre(txtNombre.getText().trim());
        paciente.setApellidos(txtApellidos.getText().trim());
        paciente.setFechaNacimiento(dpFechaNacimiento.getValue());
        paciente.setGenero(cbGenero.getValue().getCodigo());
        paciente.setEmail(txtEmail.getText().trim());
        paciente.setTelefono(txtTelefono.getText().trim());
        paciente.setTalla(txtTalla.getText().trim());
        paciente.setDomicilio(txtDomicilio.getText().trim());
        paciente.setFotografia(fotografiaBytes);

        // Convertir peso y estatura
        try {
            if (!txtPeso.getText().trim().isEmpty()) paciente.setPeso(new BigDecimal(txtPeso.getText().trim()));

            // Lógica para construir la estatura desde los ComboBoxes
            Integer metros = cbMetros.getValue();
            Integer centimetros = cbCentimetros.getValue();
            if (metros != null && centimetros != null) {
                String estaturaStr = metros + "." + String.format("%02d", centimetros);
                paciente.setEstatura(new BigDecimal(estaturaStr));
            } else {
                paciente.setEstatura(null); // Opcional: guardar null si no se selecciona
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Peso debe ser un número válido", Alert.AlertType.ERROR);
            return;
        }

        // Asignación automática de médico para nuevos pacientes
        if (esNuevo) {
            if (medicoLogueado != null) {
                paciente.setIdMedico(medicoLogueado.getIdMedico());
                paciente.setEstatus("Activo");
            } else {
                mostrarAlerta("Error Crítico", "No se ha identificado al médico. No se puede guardar el nuevo paciente.", Alert.AlertType.ERROR);
                return;
            }
        }

        // Lógica de guardado en la base de datos
        boolean exito;
        if (esNuevo) {
            exito = pacienteDAO.create(paciente);
        } else {
            exito = pacienteDAO.update(paciente);
        }

        if (exito) {
            String mensaje = esNuevo ? "Paciente creado correctamente." : "Paciente actualizado correctamente.";
            mostrarAlerta("Éxito", mensaje, Alert.AlertType.INFORMATION);
            cargarPacientes();
            deshabilitarEdicion();
        } else {
            mostrarAlerta("Error de Guardado", "No se pudo guardar la información del paciente en la base de datos.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cancelarEdicion() {
        modoEdicion = false;
        deshabilitarEdicion();
        if (pacienteSeleccionado != null) {
            mostrarDetallesPaciente(pacienteSeleccionado);
        } else {
            limpiarFormulario();
        }
    }

    @FXML
    private void cambiarFotografia() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar fotografía");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File archivo = fileChooser.showOpenDialog(btnCambiarFoto.getScene().getWindow());
        if (archivo != null) {
            try (FileInputStream fis = new FileInputStream(archivo)) {
                fotografiaBytes = fis.readAllBytes();
                Image image = new Image(new ByteArrayInputStream(fotografiaBytes));
                imgFotografia.setImage(image);
            } catch (IOException e) {
                mostrarAlerta("Error", "No se pudo cargar la imagen", Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarAlerta("Error", "El nombre es obligatorio", Alert.AlertType.ERROR);
            return false;
        }
        if (txtApellidos.getText().trim().isEmpty()) {
            mostrarAlerta("Error", "Los apellidos son obligatorios", Alert.AlertType.ERROR);
            return false;
        }
        if (dpFechaNacimiento.getValue() == null) {
            mostrarAlerta("Error", "La fecha de nacimiento es obligatoria", Alert.AlertType.ERROR);
            return false;
        }
        if (cbGenero.getValue() == null) {
            mostrarAlerta("Error", "El género es obligatorio", Alert.AlertType.ERROR);
            return false;
        }
        if (txtEmail.getText().trim().isEmpty()) {
            mostrarAlerta("Error", "El email es obligatorio", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void habilitarEdicion() {
        txtNombre.setDisable(false);
        txtApellidos.setDisable(false);
        dpFechaNacimiento.setDisable(false);
        cbGenero.setDisable(false);
        txtEmail.setDisable(false);
        txtTelefono.setDisable(false);
        txtPeso.setDisable(false);
        cbMetros.setDisable(false);
        cbCentimetros.setDisable(false);
        txtTalla.setDisable(false);
        txtDomicilio.setDisable(false);
        btnCambiarFoto.setDisable(false);
        btnGuardar.setDisable(false);
        btnCancelar.setDisable(false);

        // Deshabilitar otros botones durante edición
        btnNuevo.setDisable(true);
        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);
        btnBuscar.setDisable(true);
        btnLimpiar.setDisable(true);
    }

    private void deshabilitarEdicion() {
        txtNombre.setDisable(true);
        txtApellidos.setDisable(true);
        dpFechaNacimiento.setDisable(true);
        cbGenero.setDisable(true);
        txtEmail.setDisable(true);
        txtTelefono.setDisable(true);
        txtPeso.setDisable(true);
        cbMetros.setDisable(true);
        cbCentimetros.setDisable(true);
        txtTalla.setDisable(true);
        txtDomicilio.setDisable(true);
        btnCambiarFoto.setDisable(true);
        btnGuardar.setDisable(true);
        btnCancelar.setDisable(true);

        // Habilitar otros botones
        btnNuevo.setDisable(false);
        btnVerHistorial.setDisable(pacienteSeleccionado == null);
        btnEditar.setDisable(pacienteSeleccionado == null);
        btnEliminar.setDisable(pacienteSeleccionado == null);
        btnBuscar.setDisable(false);
        btnLimpiar.setDisable(false);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // Dentro de PacienteController.java

    @FXML
    private void verHistorialPaciente() {
        if (pacienteSeleccionado == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fitness/historial-completo-view.fxml"));
            Parent root = loader.load();

            HistorialCompletoController controller = loader.getController();
            controller.setPaciente(pacienteSeleccionado);

            Stage stage = new Stage();
            stage.setTitle("Historial Completo de " + pacienteSeleccionado.getNombre());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}