
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println(
                    "Usage: java Main <image.jpg> <squareSize> <S|M>"
            );
            return;
        }

        String fileName = args[0];

        if (!fileName.toLowerCase().endsWith(".jpg")
                && !fileName.toLowerCase().endsWith(".jpeg")) {
            System.err.println("Input must be a JPG image.");
            return;
        }

        int squareSize;

        try {
            squareSize = Integer.parseInt(args[1]);

            if (squareSize <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.err.println(
                    "Square size must be a positive integer."
            );
            return;
        }

        ProcessingMode mode;

        try {
            mode = ProcessingMode.valueOf(args[2]);
        } catch (IllegalArgumentException e) {
            System.err.println(
                    "Processing mode must be S or M."
            );
            return;
        }

        try {
            BufferedImage image = ImageIO.read(
                    new File(fileName)
            );

            if (image == null) {
                System.err.println(
                        "Could not read the image."
                );
                return;
            }

            System.out.println(
                    "Image: " + image.getWidth()
                    + "x" + image.getHeight()
            );
            System.out.println("Square size: " + squareSize);
            System.out.println("Processing mode: " + mode);

            SwingUtilities.invokeLater(() -> {
                ImageWindow window = new ImageWindow(
                        image, squareSize
                );
                window.setVisible(true);
                window.startProcessing();
            });

        } catch (IOException e) {
            System.err.println(
                    "Error loading image: " + e.getMessage()
            );
        }
    }
}
