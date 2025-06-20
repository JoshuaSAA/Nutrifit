package com.example.fitness.Controller;

import com.example.fitness.DAO.MedicoDAO;
import com.example.fitness.model.Medico;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.example.fitness.model.GeneroItem;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MedicoController {

    // Controles de la interfaz
    @FXML private TableView<Medico> tablaMedicos;
    @FXML private TableColumn<Medico, Integer> colId;
    @FXML private TableColumn<Medico, String> colNombre;
    @FXML private TableColumn<Medico, String> colApellidos;
    @FXML private TableColumn<Medico, String> colNumeroPersonal;
    @FXML private TableColumn<Medico, String> colCedula;
    @FXML private TableColumn<Medico, String> colEstatus;

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private ComboBox<GeneroItem> cbGenero; // <-- TIPO CORREGIDO
    @FXML private TextField txtDomicilio;
    @FXML private TextField txtNumeroPersonal;
    @FXML private TextField txtCedula;
    @FXML private PasswordField txtContrasena;
    @FXML private Label lblContrasena;

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
        medicoDAO = new MedicoDAO();
        medicosList = FXCollections.observableArrayList();

        configurarTabla();
        cbGenero.setItems(generos);
        cargarMedicos();

        tablaMedicos.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> mostrarDetallesMedico(newVal));
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idMedico"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colNumeroPersonal.setCellValueFactory(new PropertyValueFactory<>("numeroPersonal"));
        colCedula.setCellValueFactory(new PropertyValueFactory<>("cedulaProfesional"));
        colEstatus.setCellValueFactory(new PropertyValueFactory<>("estatus"));
        tablaMedicos.setItems(medicosList);
    }

    private void cargarMedicos() {
        medicosList.setAll(medicoDAO.readAll());
    }

    private void mostrarDetallesMedico(Medico medico) {
        medicoSeleccionado = medico;
        if (medico != null) {
            txtNombre.setText(medico.getNombre());
            txtApellidos.setText(medico.getApellidos());
            dpFechaNacimiento.setValue(medico.getFechaNacimiento());
            // Lógica corregida para seleccionar el GeneroItem correcto
            Optional<GeneroItem> generoSeleccionado = generos.stream()
                    .filter(item -> item.getCodigo().equals(medico.getGenero()))
                    .findFirst();
            generoSeleccionado.ifPresent(cbGenero::setValue);
            txtDomicilio.setText(medico.getDomicilio());
            txtNumeroPersonal.setText(medico.getNumeroPersonal());
            txtCedula.setText(medico.getCedulaProfesional());
            // Por seguridad, no mostramos la contraseña.
            // La hacemos opcional al editar.
            txtContrasena.setVisible(false);
            lblContrasena.setVisible(false);
            txtContrasena.clear();
        } else {
            limpiarFormulario();
        }
    }

    private void limpiarFormulario() {
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
        // Validación básica
        if (txtNombre.getText().isEmpty() || txtNumeroPersonal.getText().isEmpty()) {
            mostrarAlerta("Error de Validación", "El nombre y el número de personal son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        LocalDate fechaNacimiento = dpFechaNacimiento.getValue();
        if (fechaNacimiento.plusYears(18).isAfter(LocalDate.now())) {
            mostrarAlerta("Error de Validación", "El médico debe tener al menos 18 años de edad.", Alert.AlertType.WARNING);
            return;
        }

        // Si es un médico nuevo, la contraseña es obligatoria
        if (medicoSeleccionado == null && txtContrasena.getText().isEmpty()) {
            mostrarAlerta("Error de Validación", "La contraseña es obligatoria para nuevos médicos.", Alert.AlertType.WARNING);
            return;
        }

        // Crear o actualizar el objeto Medico
        boolean esNuevo = (medicoSeleccionado == null);
        Medico medico = esNuevo ? new Medico() : medicoSeleccionado;

        medico.setNombre(txtNombre.getText());
        medico.setApellidos(txtApellidos.getText());
        medico.setFechaNacimiento(dpFechaNacimiento.getValue());
        medico.setGenero(cbGenero.getValue().getCodigo());
        medico.setDomicilio(txtDomicilio.getText());
        medico.setNumeroPersonal(txtNumeroPersonal.getText());
        medico.setCedulaProfesional(txtCedula.getText());
        medico.setEstatus("Activo"); // Por defecto

        // La contraseña solo se asigna si es un médico nuevo
        if (esNuevo) {
            medico.setContrasena(txtContrasena.getText());
        }

        // Guardar en la DB
        boolean exito;
        if (esNuevo) {
            exito = medicoDAO.create(medico);
        } else {
            exito = medicoDAO.update(medico);
        }

        if (exito) {
            mostrarAlerta("Éxito", "Médico guardado correctamente.", Alert.AlertType.INFORMATION);
            cargarMedicos();
            limpiarFormulario();
        } else {
            mostrarAlerta("Error", "No se pudo guardar el médico en la base de datos.", Alert.AlertType.ERROR);
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
            if (medicoDAO.delete(medicoSeleccionado.getIdMedico())) {
                mostrarAlerta("Éxito", "El médico ha sido dado de baja.", Alert.AlertType.INFORMATION);
                cargarMedicos();
                limpiarFormulario();
            } else {
                mostrarAlerta("Error", "No se pudo dar de baja al médico.", Alert.AlertType.ERROR);
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
