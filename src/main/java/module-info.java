module com.example.fitness {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.fitness to javafx.fxml;
    opens com.example.fitness.Controller to javafx.fxml;
    opens database to javafx.fxml;


    opens com.example.fitness.model to javafx.base;

    exports com.example.fitness;
    exports com.example.fitness.Controller;
    exports com.example.fitness.model;
    exports database;
}