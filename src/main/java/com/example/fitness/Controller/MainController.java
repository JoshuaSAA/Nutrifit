package com.example.fitness.Controller;

import com.example.fitness.model.Medico;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane mainPane;

    @FXML
    private Label bienvenidaLabel;

    private Medico medicoLogueado;

    public void setMedico(Medico medico) {
        this.medicoLogueado = medico;
        if (medico != null) {
            System.out.println("[MainController] setMedico: OK. Recibido médico con ID = " + medico.getIdMedico());
            bienvenidaLabel.setText("Bienvenido(a), Dr(a). " + medico.getNombre() + " " + medico.getApellidos());
        } else {
            System.out.println("[MainController] setMedico: ALERTA. Se recibió un médico NULO.");
        }
    }

    @FXML
    private void handleGestionarPacientes() {
        System.out.println("Cargando vista de pacientes...");
        cargarVista("pacientes-view.fxml");
    }

    @FXML
    private void handleGestionarCitas() {
        System.out.println("Cargando vista de citas...");
        cargarVista("citas-view.fxml");
    }


    @FXML
    private void handleGestionarConsultas() {
        System.out.println("Cargando vista de consultas...");
        //cargarVista("consulta-form-view.fxml");
    }
    @FXML
    private void handleCerrarSesion() {
        try {
            // Cierra la ventana actual
            Stage stageActual = (Stage) mainPane.getScene().getWindow();
            stageActual.close();

            // Carga y muestra la ventana de login
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/example/fitness/Login-view.fxml"));
            Stage loginStage = new Stage();
            loginStage.setTitle("FIT NUTRITION - Inicio de Sesión");
            loginStage.setScene(new Scene(loginRoot));
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("No se pudo volver a la pantalla de inicio de sesión.");
            alert.showAndWait();
        }
    }

    private void cargarVista(String fxmlFileName) {
        try {
            String ruta = "/com/example/fitness/" + fxmlFileName;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ruta));
            Parent vista = loader.load();

            // =============================================================
            // AQUÍ ESTÁ LA LÓGICA AÑADIDA
            // =============================================================
            Object controller = loader.getController();
            if (controller instanceof CitasController) {
                ((CitasController) controller).setMedico(this.medicoLogueado);
            } else if (controller instanceof PacienteController) {
                ((PacienteController) controller).setMedico(this.medicoLogueado);
            }
            // =============================================================

            mainPane.setCenter(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
