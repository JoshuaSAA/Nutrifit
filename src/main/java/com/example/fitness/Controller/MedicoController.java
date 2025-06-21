package com.example.fitness.Controller;

import com.example.fitness.DAO.MedicoDAO;
import com.example.fitness.model.Medico;
import com.example.fitness.model.GeneroItem; // Asegúrate de tener este import
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.sql.SQLException; // <-- IMPORTANTE AÑADIR/VERIFICAR
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MedicoController {

    // --- Controles de la interfaz (Tu código) ---
    @FXML private TableView<Medico> tablaMedicos;
    @FXML private TableColumn<Medico, Integer> colId;
    @FXML private TableColumn<Medico, String> colNombre, colApellidos, colNumeroPersonal, colCedula, colEstatus;
    @FXML private TextField txtNombre, txtApellidos, txtDomicilio, txtNumeroPersonal, txtCedula;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private ComboBox<GeneroItem> cbGenero;
    @FXML private PasswordField txtContrasena;
    @FXML private Label lblContrasena;

    // --- Clases y Datos (Tu código) ---
    private MedicoDAO medicoDAO;
    private ObservableList<Medico> medicosList;
    private Medico medicoSeleccionado;
    private final ObservableList<GeneroItem> generos = FXCollections.observableArrayList(
            new GeneroItem("M", "Masculino"),
            new GeneroItem("F", "Femenino"),
            new GeneroItem("Otro", "Otro")
    );

    @FXML
    public void initialize() {
        // Tu initialize se queda igual
        medicoDAO = new MedicoDAO();
        medicosList = FXCollections.observableArrayList();
        configurarTabla();
        cbGenero.setItems(generos);
        final Callback<DatePicker, DateCell> dayCellFactory = datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        };
        dpFechaNacimiento.setDayCellFactory(dayCellFactory);
        cargarMedicos();
        tablaMedicos.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> mostrarDetallesMedico(newVal));
    }

    private void configurarTabla() {
        // Tu configurarTabla se queda igual
        colId.setCellValueFactory(new PropertyValueFactory<>("idMedico"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colNumeroPersonal.setCellValueFactory(new PropertyValueFactory<>("numeroPersonal"));
        colCedula.setCellValueFactory(new PropertyValueFactory<>("cedulaProfesional"));
        colEstatus.setCellValueFactory(new PropertyValueFactory<>("estatus"));
        tablaMedicos.setItems(medicosList);
    }

    private void cargarMedicos() {
        try {
            medicosList.setAll(medicoDAO.readAll());
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Carga", "No se pudieron cargar los médicos de la base de datos.", Alert.AlertType.ERROR);
        }
    }

    private void mostrarDetallesMedico(Medico medico) {
        // Tu mostrarDetallesMedico se queda igual
        medicoSeleccionado = medico;
        if (medico != null) {
            txtNombre.setText(medico.getNombre());
            txtApellidos.setText(medico.getApellidos());
            dpFechaNacimiento.setValue(medico.getFechaNacimiento());
            Optional<GeneroItem> generoSeleccionado = generos.stream()
                    .filter(item -> item.getCodigo().equals(medico.getGenero()))
                    .findFirst();
            generoSeleccionado.ifPresent(cbGenero::setValue);
            txtDomicilio.setText(medico.getDomicilio());
            txtNumeroPersonal.setText(medico.getNumeroPersonal());
            txtCedula.setText(medico.getCedulaProfesional());
            txtContrasena.setVisible(false);
            lblContrasena.setVisible(false);
            txtContrasena.clear();
        } else {
            limpiarFormulario();
        }
    }

    private void limpiarFormulario() {
        // Tu limpiarFormulario se queda igual
        medicoSeleccionado = null;
        txtNombre.clear();
        txtApellidos.clear();
        dpFechaNacimiento.setValue(null);
        cbGenero.setValue(null);
        txtDomicilio.clear();
        txtNumeroPersonal.clear();
        txtCedula.clear();
        txtContrasena.clear();
        txtContrasena.setVisible(true);
        lblContrasena.setVisible(true);
        tablaMedicos.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleNuevo() {
        limpiarFormulario();
    }

    @FXML
    private void handleGuardar() {
        // Tus validaciones se quedan igual
        if (txtNombre.getText().isEmpty() || txtNumeroPersonal.getText().isEmpty() || dpFechaNacimiento.getValue() == null || cbGenero.getValue() == null) {
            mostrarAlerta("Error de Validación", "Todos los campos son obligatorios.", Alert.AlertType.WARNING);
            return;
        }
        LocalDate fechaNacimiento = dpFechaNacimiento.getValue();
        if (fechaNacimiento.plusYears(18).isAfter(LocalDate.now())) {
            mostrarAlerta("Error de Validación", "El médico debe tener al menos 18 años.", Alert.AlertType.WARNING);
            return;
        }
        boolean esNuevo = (medicoSeleccionado == null);
        if (esNuevo && txtContrasena.getText().isEmpty()) {
            mostrarAlerta("Error de Validación", "La contraseña es obligatoria para nuevos médicos.", Alert.AlertType.WARNING);
            return;
        }

        // Tu lógica de creación de objeto se queda igual
        Medico medico = esNuevo ? new Medico() : medicoSeleccionado;
        medico.setNombre(txtNombre.getText());
        medico.setApellidos(txtApellidos.getText());
        medico.setFechaNacimiento(dpFechaNacimiento.getValue());
        medico.setGenero(cbGenero.getValue().getCodigo());
        medico.setDomicilio(txtDomicilio.getText());
        medico.setNumeroPersonal(txtNumeroPersonal.getText());
        medico.setCedulaProfesional(txtCedula.getText());
        medico.setEstatus("Activo");
        if (esNuevo) {
            medico.setContrasena(txtContrasena.getText());
        }

        // =============================================================
        // LÓGICA DE GUARDADO ACTUALIZADA CON MANEJO DE ERRORES
        // =============================================================
        try {
            boolean exito = esNuevo ? medicoDAO.create(medico) : medicoDAO.update(medico);
            if (exito) {
                mostrarAlerta("Éxito", "Médico guardado correctamente.", Alert.AlertType.INFORMATION);
                cargarMedicos();
                limpiarFormulario();
            } else {
                mostrarAlerta("Aviso", "No se realizaron cambios en la base de datos.", Alert.AlertType.WARNING);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Código para "Entrada duplicada" en MySQL
                mostrarAlerta("Error de Duplicado", "Ya existe un médico con ese Número de Personal o Cédula Profesional.", Alert.AlertType.ERROR);
            } else {
                e.printStackTrace();
                mostrarAlerta("Error de Base de Datos", "No se pudo guardar el médico. Razón: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleBaja() {
        if (medicoSeleccionado == null) {
            mostrarAlerta("Sin selección", "Por favor, seleccione un médico para dar de baja.", Alert.AlertType.WARNING);
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Baja");
        confirmacion.setHeaderText("¿Está seguro de que desea dar de baja al Dr(a). " + medicoSeleccionado.getNombre() + "?");
        confirmacion.setContentText("Esta acción cambiará su estatus a 'Inactivo'.");
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                if (medicoDAO.delete(medicoSeleccionado.getIdMedico())) {
                    mostrarAlerta("Éxito", "El médico ha sido dado de baja.", Alert.AlertType.INFORMATION);
                    cargarMedicos();
                    limpiarFormulario();
                } else {
                    mostrarAlerta("Error", "No se pudo dar de baja al médico.", Alert.AlertType.ERROR);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error de Base de Datos", "Ocurrió un error al intentar dar de baja al médico.", Alert.AlertType.ERROR);
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
