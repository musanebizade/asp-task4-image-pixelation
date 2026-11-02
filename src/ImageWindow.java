
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ImageWindow extends JFrame {

    private final BufferedImage original;
    private final BufferedImage result;
    private final JPanel imagePanel;
    private final JLabel statusLabel;

    private final int squareSize;
    private final ProcessingMode mode;

    private final ConcurrentLinkedQueue<Block> completedBlocks =
            new ConcurrentLinkedQueue<>();

    private final long totalBlocks;
    private final int workerCount;

    private long displayedBlocks = 0;
    private long processingTime = 0;
    private boolean processingFinished = false;

    private Timer displayTimer;

    // Stores the information required to display one block.
    private record Block(
            int x, int y, int width, int height, int color) {}

    public ImageWindow(
            BufferedImage image,
            int squareSize,
            ProcessingMode mode) {

        this.original = image;
        this.squareSize = squareSize;
        this.mode = mode;

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

        int availableCores =
                Runtime.getRuntime().availableProcessors();

        workerCount = mode == ProcessingMode.S
                ? 1
                : (int) Math.min(
                        Math.max(1, availableCores), rows);

        setTitle("Image Pixelation - " + mode);
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

        displayTimer = new Timer(33, e -> updateDisplay());
        displayTimer.start();

        SwingWorker<Long, Void> worker = new SwingWorker<>() {

            @Override
            protected Long doInBackground() throws Exception {

                long start = System.nanoTime();

                if (mode == ProcessingMode.S) {
                    processRegion(
                            0,
                            ((long) original.getHeight() - 1)
                                    / squareSize + 1
                    );
                } else {
                    processParallel();
                }

                return System.nanoTime() - start;
            }

            @Override
            protected void done() {
                try {
                    processingTime = get();
                    processingFinished = true;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    displayTimer.stop();
                    statusLabel.setText("Processing interrupted");
                } catch (ExecutionException | CancellationException e) {
                    displayTimer.stop();
                    statusLabel.setText(
                            "Processing failed: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    // Processes a consecutive range of block rows.
    private void processRegion(long startRow, long endRow) {

        int width = original.getWidth();
        int height = original.getHeight();

        for (long row = startRow; row < endRow; row++) {

            long y = row * squareSize;
            int startY = (int) y;

            for (long x = 0; x < width; x += squareSize) {

                int startX = (int) x;

                int blockWidth = (int) Math.min(
                        (long) squareSize, width - x);

                int blockHeight = (int) Math.min(
                        (long) squareSize, height - y);

                int color = ImageProcessor.averageColor(
                        original, startX, startY, squareSize);

                completedBlocks.add(new Block(
                        startX,
                        startY,
                        blockWidth,
                        blockHeight,
                        color
                ));
            }
        }
    }

    // Divides the block rows among multiple workers.
    private void processParallel()
            throws InterruptedException, ExecutionException {

        long totalRows =
                ((long) original.getHeight() - 1)
                        / squareSize + 1;

        ExecutorService executor =
                Executors.newFixedThreadPool(workerCount);

        List<Future<?>> tasks = new ArrayList<>();

        try {
            for (int i = 0; i < workerCount; i++) {

                long startRow =
                        totalRows * i / workerCount;

                long endRow =
                        totalRows * (i + 1) / workerCount;

                tasks.add(executor.submit(() ->
                        processRegion(startRow, endRow)
                ));
            }

            // Wait until all processing workers finish.
            for (Future<?> task : tasks) {
                task.get();
            }

        } finally {
            executor.shutdownNow();
        }
    }

    // Runs on Swing's Event Dispatch Thread.
    private void updateDisplay() {

        long blocksPerFrame = Math.max(
                1, totalBlocks / 150);

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
                "Mode: %s | Workers: %d | Progress: %.1f%%",
                mode, workerCount, progress
        ));

        if (processingFinished && completedBlocks.isEmpty()) {

            displayTimer.stop();
            imagePanel.repaint();

            statusLabel.setText(String.format(
                    "Completed | Mode: %s | Workers: %d | "
                            + "Processing time: %.2f ms",
                    mode,
                    workerCount,
                    processingTime / 1_000_000.0
            ));

            setTitle("Image Pixelation - Completed");
        }
    }
}
