package newt.clipper.ffmpeg;

import com.github.kokorin.jaffree.ffmpeg.*;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import newt.clipper.Main;
import newt.clipper.ui.Utils;

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

    private static final URL ffmpegPath;
    private static final URL ffprobePath;

    static {
        ffmpegPath = Objects.requireNonNull(Main.class.getResource("ffmpeg.exe"));
        ffprobePath = Objects.requireNonNull(Main.class.getResource("ffprobe.exe"));
    }

    public void cutClip(File currentFile, String newClipName, String startTime, String endTime) throws URISyntaxException {
        FFmpeg ffmpeg = new FFmpeg(Paths.get(ffmpegPath.toURI()));
        String name = currentFile.getName();
        ffmpeg.addArgument("-ss")
                .addArgument(startTime)
                .addArgument("-to")
                .addArgument(endTime)
                .addInput(UrlInput.fromPath(currentFile.toPath()))
                .addOutput(UrlOutput.toPath(Path.of(newClipName))
                        .setFormat(name.substring(name.lastIndexOf(".") + 1))     // defina o formato se quiser garantir a extensão correta
                        .addArgument("-c")
                        .addArgument("copy"))
                .execute();
    }


    public BufferedImage getVideoThumbnail(File currentFile) throws IOException, URISyntaxException {
        FFmpeg ffmpeg = new FFmpeg(Paths.get(ffmpegPath.toURI()));
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

    public static String getVideoDuration(Path videoPath) throws URISyntaxException {

        FFprobe fFprobe = new FFprobe(Paths.get(ffprobePath.toURI()));

        FFprobeResult result =  fFprobe.setInput(videoPath)
                                .setShowFormat(true)
                                .execute();

        double durationSeconds = result.getFormat().getDuration();

        return Utils.formatDuration(durationSeconds);
    }


}
