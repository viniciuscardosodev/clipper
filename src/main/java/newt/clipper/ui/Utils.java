package newt.clipper.ui;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Random;

public abstract class Utils {

    public static File currentFile;

    public static String formatDuration(double totalSeconds) {
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        int seconds = (int) (totalSeconds % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static int toSeconds(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }

    public static String checkCompletePath(String s) {
        Path possibleCompletePath;
        try {
            possibleCompletePath = Paths.get(s.substring(0, s.lastIndexOf("\\")));
            if (Files.exists(possibleCompletePath)) {
                return s + getCurrentExtension();
            } else {
                return generateClipName() + s + getCurrentExtension();
            }
        } catch (StringIndexOutOfBoundsException e) {
            return getCurrentVideoFolder() + "\\" + s + getCurrentExtension();
        }
    }

    public static String generateClipName() {
        return getCurrentVideoFolder() + "\\" + generateHashID(String.valueOf(new Random().nextInt(1000))) + getCurrentExtension();
    }

    public static Path getCurrentVideoFolder(){
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String userHome = System.getProperty("user.home");

        Path videos;

        if (os.contains("win")) {
            videos = Paths.get(userHome, "Videos");
        } else if (os.contains("mac")) {
            videos = Paths.get(userHome, "Movies");
        } else if (os.contains("nix") || os.contains("nux")) {
            videos = Paths.get(userHome, "Vídeos");
            if (!Files.exists(videos)) {
                videos = Paths.get(userHome, "Videos");
            }
        } else {
            throw new UnsupportedOperationException("Sistema operacional não suportado: " + os);
        }

        Path clipCutter = videos.resolve("ClipCutter");
        certifyCreated(clipCutter);
        return clipCutter;
    }

    public static void certifyCreated(Path clipCutter) {
        if (!Files.exists(clipCutter))
            if (clipCutter.toFile().mkdirs())
                System.out.println("Criado com sucesso!");
            else
                System.out.println("Falha ao criar com sucesso!");
    }

    private static String getCurrentExtension() {
        return currentFile.getName().substring(currentFile.getName().lastIndexOf("."));
    }

    public static String generateHashID(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convertendo os primeiros bytes para hex
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < 6; i++) { // 6 bytes = 12 caracteres hexadecimais
                String hex = Integer.toHexString(0xff & hashBytes[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash", e);
        }
    }


    public static boolean isBefore(String time1, String time2) {
        // Converte as minutagens em segundos
        int seconds1 = toSeconds(time1);
        int seconds2 = toSeconds(time2);

        return seconds1 < seconds2;
    }

    public static TextFormatter<?> getFileNameFormatter() {
        return new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            // Permitir apenas o formato: números e ":" no lugar correto
            if (newText.matches("^[^<>:\"/\\\\|?*]*$")) {
                return change; // aceito
            } else {
                return null; // rejeita a mudança
            }
        });
    }

    public static TextFormatter<String> getTimeFormatter() {
        return new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            // Permitir apenas o formato: números e ":" no lugar correto
            if (newText.matches("^\\d{0,2}:?\\d{0,2}:?\\d{0,2}$")) {
                return change; // aceito
            } else {
                return null; // rejeita a mudança
            }
        });
    }

    public static void format(boolean newVal, TextField textField) {
        if (!newVal) { // Quando perde o foco
            String text = textField.getText();
            String[] parts = text.split(":");
            if (parts.length == 1) {
                String p1 = text.substring(0, 2);
                String p2 = text.substring(2, 4);
                String p3 = text.substring(4, 6);
                text = p1 + ":" + p2 + ":" + p3;
                System.out.println(text);
            }
            parts = text.split(":");
            while (parts.length < 3) {
                text = "00:" + text;
                parts = text.split(":");
            }
            if (parts.length == 3) {
                // Se cada parte for menor que 2 dígitos, preenche com 0
                String formatted = String.format("%02d:%02d:%02d",
                        parsePart(parts[0]), parsePart(parts[1]), parsePart(parts[2]));
                textField.setText(formatted);
            }
        }
    }

    public static int parsePart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static void updateImage(BufferedImage bufferedImage, ImageView imageView) {
        Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
        // Como provavelmente você está em uma thread fora do JavaFX:
        Platform.runLater(() -> {
            imageView.setImage(fxImage);
        });
    }

}
