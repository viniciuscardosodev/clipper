package newt.clipper.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import newt.clipper.Main;

import java.io.IOException;
import java.util.Objects;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("index.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 480, 240);
        stage.setTitle("Clipper");
        stage.setScene(scene);
        stage.getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("logo.png"))));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}