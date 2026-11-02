
public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java Main <image.jpg> <squareSize> <S|M>");
            return;
        }

        String fileName = args[0];
        int squareSize;

        try {
            squareSize = Integer.parseInt(args[1]);
            if (squareSize <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.err.println("Square size must be a positive integer.");
            return;
        }

        ProcessingMode mode;

        try {
            mode = ProcessingMode.valueOf(args[2]);
        } catch (IllegalArgumentException e) {
            System.err.println("Processing mode must be S or M.");
            return;
        }

        System.out.println("Image: " + fileName);
        System.out.println("Square size: " + squareSize);
        System.out.println("Mode: " + mode);
        System.out.println("Project initialized successfully!");
    }
}
