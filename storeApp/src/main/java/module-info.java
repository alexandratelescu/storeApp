module com.example.storeapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires java.desktop;


    opens UI to javafx.fxml;
    exports UI;
}