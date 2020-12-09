package ru.notsoold.cardcv;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import static ru.notsoold.cardcv.CardCvUtils.*;

public class CardsCutter {

    private static final double upLeftCornerX = 0.2282;
    private static final double upLeftCornerY = 0.5034;
    private static final double nextCardX = 0.1120;
    private static final double cardWidth = 0.0954;
    private static final double cardHeight = 0.0715;

    public static List<BufferedImage> cut(Path originalImage) {
        List<BufferedImage> cutCards = new ArrayList<>();
        try {
            BufferedImage image = ImageIO.read(originalImage.toFile());
            int width = image.getWidth();
            int height = image.getHeight();
            for (int i = 0; i < 5; i++) {
                Image subImage = image.getSubimage((int)(upLeftCornerX * width + nextCardX * width * i),
                                (int)(upLeftCornerY * height), (int) (cardWidth * width),
                                (int)(cardHeight * height)).getScaledInstance(70, 100, Image.SCALE_SMOOTH);
                BufferedImage outputImage = new BufferedImage(70, 100, BufferedImage.TYPE_INT_RGB);
                outputImage.getGraphics().drawImage(subImage, 0, 0, null);
                // Check if this is the actual card.
                if (rgbToHardBW(outputImage.getRGB(50, 20)) != 255) {
                    continue;
                }
                cutCards.add(outputImage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cutCards;
    }

    public static BufferedImage cutCardSuit(BufferedImage wholeCardImage) { return wholeCardImage.getSubimage(26, 58, 40, 40); }
    public static BufferedImage cutCardValue(BufferedImage wholeCardImage) { return wholeCardImage.getSubimage(5, 5, 30, 30); }
}
