
import java.awt.image.BufferedImage;

public class ImageProcessor {

    public static int averageColor(
            BufferedImage image,
            int startX,
            int startY,
            int squareSize) {

        int endX = (int) Math.min(
                (long) startX + squareSize,
                image.getWidth()
        );

        int endY = (int) Math.min(
                (long) startY + squareSize,
                image.getHeight()
        );

        long red = 0;
        long green = 0;
        long blue = 0;
        long count = 0;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int rgb = image.getRGB(x, y);

                red += (rgb >> 16) & 0xFF;
                green += (rgb >> 8) & 0xFF;
                blue += rgb & 0xFF;

                count++;
            }
        }

        int r = (int) (red / count);
        int g = (int) (green / count);
        int b = (int) (blue / count);

        return (r << 16) | (g << 8) | b;
    }

    public static BufferedImage processImage(
            BufferedImage original,
            int squareSize) {

        int width = original.getWidth();
        int height = original.getHeight();

        BufferedImage result = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_RGB
        );

        for (int y = 0; y < height; y += squareSize) {
            for (int x = 0; x < width; x += squareSize) {

                int color = averageColor(
                        original, x, y, squareSize
                );

                int endX = (int) Math.min(
                        (long) x + squareSize, width
                );
                int endY = (int) Math.min(
                        (long) y + squareSize, height
                );

                for (int py = y; py < endY; py++) {
                    for (int px = x; px < endX; px++) {
                        result.setRGB(px, py, color);
                    }
                }
            }
        }

        return result;
    }
}
