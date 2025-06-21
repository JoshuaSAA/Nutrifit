package com.example.fitness.Controller;

import com.example.fitness.DAO.MedicoDAO;
import com.example.fitness.model.Medico;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usuarioTextField;
    @FXML
    private PasswordField contrasenaPasswordField;
    @FXML
    private Button iniciarSesionButton;

    private final MedicoDAO medicoDAO = new MedicoDAO();

    @FXML
    private void handleLogin() {
        String usuario = usuarioTextField.getText();
        String contrasenaPlana = contrasenaPasswordField.getText();

        if (usuario == null || usuario.trim().isEmpty() || contrasenaPlana == null || contrasenaPlana.trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Vacíos", "Por favor, ingrese su usuario y contraseña.");
            return;
        }

        try {
            Medico medicoLogueado = medicoDAO.authenticate(usuario, contrasenaPlana);

            if (medicoLogueado != null) {
                System.out.println("Login exitoso para: " + medicoLogueado.getNombre());

                if ("ADMIN001".equals(medicoLogueado.getNumeroPersonal())) {
                    System.out.println("Usuario reconocido como Administrador. Abriendo panel de admin...");
                    abrirVentanaAdmin(medicoLogueado);
                } else {
                    System.out.println("Usuario reconocido como Médico. Abriendo panel principal...");
                    abrirVentanaPrincipal(medicoLogueado);
                }
            } else {
                System.out.println("Fallo de autenticación para usuario: " + usuario);
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Autenticación", "El usuario o la contraseña son incorrectos.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexión", "No se pudo conectar a la base de datos. Verifique su conexión y contacte al administrador.");
        }
    }

    private void abrirVentanaPrincipal(Medico medico) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fitness/main-view.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.setMedico(medico);

            mostrarNuevaVentana("FIT NUTRITION - Panel Principal", root);
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Carga", "No se pudo cargar la ventana principal.");
        }
    }

    // MÉTODO NUEVO PARA LA VENTANA DE ADMIN
    private void abrirVentanaAdmin(Medico admin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fitness/admin-dashboard-view.fxml"));
            Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setAdmin(admin);

            mostrarNuevaVentana("FIT NUTRITION - Panel de Administración", root);
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Carga", "No se pudo cargar la ventana de administración.");
        }
    }

    // MÉTODO REFACTORIZADO PARA EVITAR CÓDIGO DUPLICADO
    private void mostrarNuevaVentana(String titulo, Parent root) {
        // Cierra la ventana actual de login
        Stage stageActual = (Stage) iniciarSesionButton.getScene().getWindow();
        stageActual.close();

        // Muestra la nueva ventana
        Stage stagePrincipal = new Stage();
        stagePrincipal.setTitle(titulo);
        stagePrincipal.setScene(new Scene(root));
        stagePrincipal.setMaximized(true);
        stagePrincipal.show();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
