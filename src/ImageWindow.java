
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ImageWindow extends JFrame {

    private final BufferedImage original;
    private final BufferedImage result;
    private final JPanel imagePanel;
    private final JLabel statusLabel;

    private final int squareSize;

    private final ConcurrentLinkedQueue<Block> completedBlocks =
            new ConcurrentLinkedQueue<>();

    private long totalBlocks;
    private long displayedBlocks = 0;
    private volatile long processingTime = 0;
    private volatile boolean processingFinished = false;

    private Timer displayTimer;

    // Represents one processed image block.
    private record Block(
            int x, int y, int width, int height, int color) {}

    public ImageWindow(BufferedImage image, int squareSize) {

        this.original = image;
        this.squareSize = squareSize;

        int width = image.getWidth();
        int height = image.getHeight();

        result = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = result.createGraphics();
        graphics.drawImage(original, 0, 0, null);
        graphics.dispose();

        long columns = ((long) width - 1) / squareSize + 1;
        long rows = ((long) height - 1) / squareSize + 1;
        totalBlocks = columns * rows;

        setTitle("Image Pixelation - Single Threaded");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        Rectangle screen = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();

        int maxWidth = Math.max(1, screen.width - 100);
        int maxHeight = Math.max(1, screen.height - 150);

        double scale = Math.min(1.0, Math.min(
                (double) maxWidth / width,
                (double) maxHeight / height
        ));

        int displayWidth = Math.max(
                1, (int) (width * scale));

        int displayHeight = Math.max(
                1, (int) (height * scale));

        imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(result, 0, 0,
                        getWidth(), getHeight(), null);
            }
        };

        imagePanel.setPreferredSize(
                new Dimension(displayWidth, displayHeight));

        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(
                BorderFactory.createEmptyBorder(8, 10, 8, 10));

        add(imagePanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    public void startProcessing() {

        // Display approximately 30 frames per second.
        displayTimer = new Timer(33, e -> updateDisplay());
        displayTimer.start();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            @Override
            protected Void doInBackground() {

                long start = System.nanoTime();

                int width = original.getWidth();
                int height = original.getHeight();

                // Process blocks from left to right,
                // and from top to bottom.
                for (long y = 0; y < height; y += squareSize) {
                    for (long x = 0; x < width; x += squareSize) {

                        int startX = (int) x;
                        int startY = (int) y;

                        int blockWidth = (int) Math.min(
                                (long) squareSize, width - x);

                        int blockHeight = (int) Math.min(
                                (long) squareSize, height - y);

                        int color = ImageProcessor.averageColor(
                                original, startX, startY, squareSize);

                        completedBlocks.add(new Block(
                                startX, startY,
                                blockWidth, blockHeight, color));
                    }
                }

                processingTime = System.nanoTime() - start;
                processingFinished = true;

                return null;
            }

            @Override
            protected void done() {
                // The timer will finish displaying queued blocks.
                if (isCancelled()) {
                    statusLabel.setText("Processing cancelled");
                    displayTimer.stop();
                }
            }
        };

        worker.execute();
    }

    private void updateDisplay() {

        // Display a limited number of blocks per frame.
        // This makes progress visible without slowing computation.
        long blocksPerFrame = Math.max(
                1, (totalBlocks + 149) / 150);

        Graphics2D graphics = result.createGraphics();

        long count = 0;
        Block block;

        while (count < blocksPerFrame
                && (block = completedBlocks.poll()) != null) {

            graphics.setColor(new Color(block.color()));

            graphics.fillRect(
                    block.x(),
                    block.y(),
                    block.width(),
                    block.height()
            );

            displayedBlocks++;
            count++;
        }

        graphics.dispose();

        if (count > 0) {
            imagePanel.repaint();
        }

        double progress =
                100.0 * displayedBlocks / totalBlocks;

        statusLabel.setText(String.format(
                "Progress: %.1f%% (%d/%d blocks)",
                progress, displayedBlocks, totalBlocks
        ));

        if (processingFinished && completedBlocks.isEmpty()) {

            displayTimer.stop();
            imagePanel.repaint();

            statusLabel.setText(String.format(
                    "Completed | Processing time: %.2f ms",
                    processingTime / 1_000_000.0
            ));

            setTitle("Image Pixelation - Completed");
        }
    }
}
