package newt.clipper.ffmpeg;

import com.github.kokorin.jaffree.ffmpeg.*;
import newt.clipper.Main;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class Handler {

    private static URL ffmpegPath;

    static {
        ffmpegPath = Objects.requireNonNull(Main.class.getResource("ffmpeg.exe"));
    }

    public void cutClip(File currentFile, String newClipName, String startTime, String endTime) {
        List<String> comando = List.of(
                ffmpegPath.toString(),
                "-ss", startTime,
                "-to", endTime,
                "-i", currentFile.toPath().toString(),
                "-c", "copy", newClipName
        );

        ProcessBuilder builder = new ProcessBuilder(comando);
        builder.redirectErrorStream(true); // junta stderr com stdout
        Process processo = null;
        try {
            processo = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Lê e imprime a saída do processo
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(processo.getInputStream()))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                System.out.println(linha);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not find ffmpeg path.");
        }

        int exitCode = 0;
        try {
            exitCode = processo.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (exitCode != 0) {
            throw new RuntimeException("Erro ao cortar o vídeo. Código de saída: " + exitCode);
        }
    }


    public BufferedImage getVideoThumbnail(File currentFile) throws IOException {
        FFmpeg ffmpeg = new FFmpeg(Paths.get(ffmpegPath.toString()));
        final BufferedImage[] bfImage = new BufferedImage[1]; // Usar array para contornar a limitação de variável final

        ffmpeg.addInput(UrlInput.fromPath(Paths.get(currentFile.getAbsolutePath())))
                .addArguments("-ss", "00:00:01")
                .addArguments("-frames:v", "1")
                .addArguments("-q:v", "2")
                .addOutput(FrameOutput.withConsumer(new FrameConsumer() {
                    @Override
                    public void consumeStreams(List<Stream> list) {
                        // Ignorado
                    }

                    @Override
                    public void consume(Frame frame) {
                        if (frame != null) {
                            System.out.println("Frame got: " + frame);
                            var image = frame.getImage();
                            if (image != null) {
                                bfImage[0] = image;
                            }
                        }
                    }
                }))
                .execute();

        return bfImage[0];
    }
}
