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

public class AdminDashboardController {

    @FXML
    private BorderPane adminPane;
    @FXML
    private Label bienvenidaLabel;

    private Medico adminLogueado;

    public void setAdmin(Medico admin) {
        this.adminLogueado = admin;
        if (admin != null) {
            bienvenidaLabel.setText("Bienvenido, " + admin.getNombre());
        }
    }

    @FXML
    private void handleGestionarMedicos() {
        System.out.println("Cargando vista para gestionar médicos...");
        try {
            Parent vista = FXMLLoader.load(getClass().getResource("/com/example/fitness/medicos-view.fxml"));
            adminPane.setCenter(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCerrarSesion() {
        try {
            // Cierra la ventana actual
            Stage stageActual = (Stage) adminPane.getScene().getWindow();
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
}
