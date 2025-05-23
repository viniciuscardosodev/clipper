package newt.clipper.ui;

import com.github.kokorin.jaffree.ffmpeg.*;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import newt.clipper.Main;
import newt.clipper.ffmpeg.Handler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static newt.clipper.ui.Utils.*;

public class Controller {

    @FXML
    private Label clipNameLabel;
    @FXML
    private CheckBox deleteCheckbox;
    @FXML
    private Label endLabel;
    @FXML
    private TextField endTimeFieldText;
    @FXML
    private Label startLabel;
    @FXML
    private TextField startTimeFieldText;
    @FXML
    private Label titleLabel;
    @FXML
    private ImageView imageView;
    @FXML
    private TextField clipNameTextField;

    private FileChooser fileChooser;
    private Alert errorTimeAlert;
    private Alert nullVideoAlert;
    private Alert cannotCreateDirectoryAlert;

    private Handler handler;

    @FXML
    public void initialize() throws URISyntaxException {
        // Código que será executado logo após os componentes do FXML serem carregados
        startTimeFieldText.setText("00:00:00");
        endTimeFieldText.setText("00:00:00");
        startTimeFieldText.focusedProperty().addListener((obs, oldVal, newVal) -> format(newVal, startTimeFieldText));
        endTimeFieldText.focusedProperty().addListener((obs, oldVal, newVal) -> format(newVal, endTimeFieldText));
        endTimeFieldText.setTextFormatter(getTimeFormatter());
        startTimeFieldText.setTextFormatter(getTimeFormatter());
        clipNameTextField.setTextFormatter(getFileNameFormatter());

        fileChooser = new FileChooser();
        fileChooser.setTitle("Select a clip");

        var filter = new FileChooser.ExtensionFilter("Video Files", Arrays.asList(
                "*.mp4", "*.mkv", "*.avi", "*.mov", "*.wmv", "*.flv", "*.webm", "*.ts", "*.mpeg", "*.mpg", "*.m4v","*.3gp"));

        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "\\Videos"));

        loadAlerts();
        handler = new Handler();
        var videoPath = Main.videoPath;
        if (videoPath != null) {
            updateImageView(videoPath.toFile());
            var total = Handler.getVideoDuration(videoPath);
            endTimeFieldText.setText(total);
        }

    }

    private void updateImageView(File file) {
        try {
            Image fxImage = SwingFXUtils.toFXImage(handler.getVideoThumbnail(file), null);
            Platform.runLater(() -> {
                imageView.setImage(fxImage);
            });
            System.out.println(Path.of(currentFile.getAbsolutePath()));
            var total = Handler.getVideoDuration(Path.of(currentFile.getAbsolutePath()));
            endTimeFieldText.setText(total);
        } catch (IOException | URISyntaxException e ) {
            System.out.println(e.getMessage());
            cannotCreateDirectoryAlert.showAndWait();
        }
    }

    @FXML
    void onCutButtonClicked(ActionEvent event) {
        if (currentFile == null) {
            nullVideoAlert.showAndWait();
            return;
        }

        String time1 = startTimeFieldText.getText();
        String time2 = endTimeFieldText.getText();

        if (!isBefore(time1, time2)) {
            errorTimeAlert.showAndWait();
            endTimeFieldText.setText(time1);
            startTimeFieldText.setText(time2);
            return;
        }

        String clipName = clipNameTextField.getText();
        String newClipName = (clipName == null || clipName.isBlank()) ? generateClipName() : checkCompletePath(clipName);

        try {
            handler.cutClip(currentFile, newClipName, startTimeFieldText.getText(), endTimeFieldText.getText());
        } catch (URISyntaxException e) {
            cannotCreateDirectoryAlert.showAndWait();
            return;
        }

        if (deleteCheckbox.isSelected()) {
            currentFile.delete();
        }


        imageView.setImage(new Image(Objects.requireNonNull(Main.class.getResource("multimedia.png")).toString()));
    }

    @FXML
    void onViewButtonClicked(MouseEvent event) {
        var f = fileChooser.showOpenDialog(null);
        if (f != null) {
            currentFile = f;
            updateImageView(f);
        }
    }

    private void loadAlerts() {
        errorTimeAlert = new Alert(Alert.AlertType.ERROR);
        errorTimeAlert.setTitle("Erro - Tempos incoerentes");
        errorTimeAlert.setHeaderText(null);
        errorTimeAlert.setContentText("O tempo \"start\" não pode ser maior que o tempo \"end\".");
        Stage alertStage = (Stage) errorTimeAlert.getDialogPane().getScene().getWindow();

        var image = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("error.png")));
        alertStage.getIcons().add(image);

        nullVideoAlert = new Alert(Alert.AlertType.ERROR);
        nullVideoAlert.setTitle("Alerta - Nenhum arquivo selecionado");
        nullVideoAlert.setHeaderText(null);
        nullVideoAlert.setContentText("Você precisa selecionar um arquivo de vídeo para fazer o clip!");

        alertStage = (Stage) nullVideoAlert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(image);

        cannotCreateDirectoryAlert = new Alert(Alert.AlertType.ERROR);
        cannotCreateDirectoryAlert.setTitle("Alerta - Escrita Negada");
        cannotCreateDirectoryAlert.setHeaderText(null);
        cannotCreateDirectoryAlert.setContentText("Não foi possível criar a pasta padrão para gravar o clip, por favor escolha um local para salvamento do clip.");
    }

}