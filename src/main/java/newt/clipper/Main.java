package newt.clipper;

import newt.clipper.ui.Application;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static Path videoPath;

    public static void main(String[] args) {
        if (args.length != 0) {
            videoPath = Paths.get(args[0]);
        }
        Application.main(args);
    }

}
