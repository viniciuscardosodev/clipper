module newt.clipper {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires javafx.swing;
    requires com.github.kokorin.jaffree;

    opens newt.clipper to javafx.fxml;
    exports newt.clipper;
    exports newt.clipper.ui;
    opens newt.clipper.ui to javafx.fxml;
}