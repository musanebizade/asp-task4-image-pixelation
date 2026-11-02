
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

        if (original == null || squareSize <= 0) {
            throw new IllegalArgumentException(
                    "Invalid image or square size");
        }

        int width = original.getWidth();
        int height = original.getHeight();

        BufferedImage result = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_RGB);

        for (long y = 0; y < height; y += squareSize) {
            for (long x = 0; x < width; x += squareSize) {

                int startX = (int) x;
                int startY = (int) y;

                int color = averageColor(
                        original, startX, startY, squareSize);

                int endX = (int) Math.min(
                        x + squareSize, width);

                int endY = (int) Math.min(
                        y + squareSize, height);

                for (int py = startY; py < endY; py++) {
                    for (int px = startX; px < endX; px++) {
                        result.setRGB(px, py, color);
                    }
                }
            }
        }

        return result;
    }
}
