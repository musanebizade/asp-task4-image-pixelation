
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageWindow extends JFrame {
    private final BufferedImage image;

    public ImageWindow(BufferedImage image) {
        this.image = image;

        setTitle("Image Pixelation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension screen = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getMaximumWindowBounds()
                .getSize();

        int maxWidth = Math.max(1, screen.width - 100);
        int maxHeight = Math.max(1, screen.height - 150);

        double scale = Math.min(
                1.0,
                Math.min(
                        (double) maxWidth / image.getWidth(),
                        (double) maxHeight / image.getHeight()
                )
        );

        int displayWidth = Math.max(
                1, (int) (image.getWidth() * scale)
        );
        int displayHeight = Math.max(
                1, (int) (image.getHeight() * scale)
        );

        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0,
                        getWidth(), getHeight(), null);
            }
        };

        imagePanel.setPreferredSize(
                new Dimension(displayWidth, displayHeight)
        );

        add(imagePanel);
        pack();
        setLocationRelativeTo(null);
    }
}
