
import java.awt.image.BufferedImage;

public class ImageProcessorTest {

    public static void main(String[] args) {

        testRegularBlocks();
        testPartialBlocks();
        testSmallImage();
        testSquareSizeOne();

        System.out.println("All tests passed!");
    }

    private static void testRegularBlocks() {

        BufferedImage image = createImage(100, 100);

        BufferedImage result =
                ImageProcessor.processImage(image, 20);

        checkDimensions(image, result);

        System.out.println("Regular blocks: PASSED");
    }

    private static void testPartialBlocks() {

        BufferedImage image = createImage(103, 77);

        BufferedImage result =
                ImageProcessor.processImage(image, 20);

        checkDimensions(image, result);

        // Check the bottom-right partial block.
        int color = result.getRGB(102, 76);

        for (int y = 60; y < 77; y++) {
            for (int x = 100; x < 103; x++) {

                if (result.getRGB(x, y) != color) {
                    throw new AssertionError(
                            "Partial block has inconsistent colors");
                }
            }
        }

        System.out.println("Partial blocks: PASSED");
    }

    private static void testSmallImage() {

        BufferedImage image = createImage(15, 15);

        BufferedImage result =
                ImageProcessor.processImage(image, 20);

        checkDimensions(image, result);

        int color = result.getRGB(0, 0);

        for (int y = 0; y < 15; y++) {
            for (int x = 0; x < 15; x++) {

                if (result.getRGB(x, y) != color) {
                    throw new AssertionError(
                            "Small image should be one block");
                }
            }
        }

        System.out.println("Small image: PASSED");
    }

    private static void testSquareSizeOne() {

        BufferedImage image = createImage(25, 25);

        BufferedImage result =
                ImageProcessor.processImage(image, 1);

        checkDimensions(image, result);

        for (int y = 0; y < 25; y++) {
            for (int x = 0; x < 25; x++) {

                if (image.getRGB(x, y) != result.getRGB(x, y)) {
                    throw new AssertionError(
                            "Square size 1 changed a pixel");
                }
            }
        }

        System.out.println("Square size 1: PASSED");
    }

    private static BufferedImage createImage(
            int width, int height) {

        BufferedImage image = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int red = (x * 3) % 256;
                int green = (y * 5) % 256;
                int blue = (x + y) % 256;

                int rgb = (red << 16)
                        | (green << 8)
                        | blue;

                image.setRGB(x, y, rgb);
            }
        }

        return image;
    }

    private static void checkDimensions(
            BufferedImage original,
            BufferedImage result) {

        if (original.getWidth() != result.getWidth()
                || original.getHeight() != result.getHeight()) {

            throw new AssertionError(
                    "Image dimensions have changed");
        }
    }

}
