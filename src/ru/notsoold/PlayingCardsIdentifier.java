package ru.notsoold;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PlayingCardsIdentifier {

    private static List<List<int[]>> describingPoints = Arrays.asList(
        Arrays.asList(new int[] {10, 10}), // A
        Arrays.asList(new int[] {10, 10}), // 1
        Arrays.asList(new int[] {10, 10}), // 2
        Arrays.asList(new int[] {10, 10}), // 3
        Arrays.asList(new int[] {25, 10}, new int[] {25, 17}, new int[] {25, 33}, new int[] {25, 27}, new int[] {30, 26}, new int[] {12, 26}), // 4
        Arrays.asList(new int[] {10, 10}), // 5
        Arrays.asList(new int[] {25, 10}, new int[] {18, 9}, new int[] {13, 19}, new int[] {15, 31}, new int[] {27, 27}, new int[] {23, 19}, new int[] {11, 22}), // 6
        Arrays.asList(new int[] {12, 9}, new int[] {19, 9}, new int[] {25, 9}, new int[] {22, 19}, new int[] {18, 25}, new int[] {15, 32}), // 7
        Arrays.asList(new int[] {16, 21}, new int[] {22, 21}, new int[] {27, 26}, new int[] {12, 30}, new int[] {25, 12}, new int[] {16, 10}), // 8
        Arrays.asList(new int[] {10, 10}), // 9
        Arrays.asList(new int[] {10, 10}), // 10
        Arrays.asList(new int[] {10, 10}), // J
        Arrays.asList(new int[] {24, 10}, new int[] {24, 32}, new int[] {15, 29}, new int[] {31, 29}, new int[] {26, 25}, new int[] {13, 20}), // Q
        Arrays.asList(new int[] {10, 10})  // K
    );
    private static Map<Integer, String> cardValueMappings = new HashMap<Integer, String>() {{
        put(0, "K"); put(1, "A"); put(2, "2"); put(3, "3"); put(4, "4"); put(5, "5"); put(6, "6"); put(7, "7"); put(8, "8"); put(9, "9");
        put(10, "10"); put(11, "J"); put(12, "Q"); put(13, "K"); put(-1, "invalid");
    }};

    public static void main(String[] args) throws Exception {
        Files.list(Paths.get("C:\\Users\\dsomov\\Downloads\\java_test_task\\imgs")).forEach(imageFile -> {
            try {
                long start = System.currentTimeMillis();
                BufferedImage image = ImageIO.read(imageFile.toFile());
                int width = image.getWidth();
                int height = image.getHeight();
                double upLeftCornerX = 0.2282;
                double upLeftCornerY = 0.5034;
                double nextCardX = 0.1120;
                double cardWidth = 0.0954;
                double cardHeight = 0.0715;
                Files.createDirectory(new File("CSSSRCase\\src\\" + imageFile.getFileName().toString().substring(0, 20)).toPath());
                for (int i = 0; i < 5; i++) {
                    Image subImage = image.getSubimage((int)(upLeftCornerX * width + nextCardX * width * i), (int)(upLeftCornerY * height), (int)(cardWidth * width), (int)(cardHeight * height)).getScaledInstance(70, 100, Image.SCALE_SMOOTH);
                    final BufferedImage outputImage = new BufferedImage(70, 100, BufferedImage.TYPE_INT_RGB);
                    outputImage.getGraphics().drawImage(subImage, 0, 0, null);
                    // Check if this is the actual card.
                    if (!new Color(outputImage.getRGB(50, 20)).equals(Color.WHITE)) {
                        continue;
                    }
                    // Find out card suit.
                    final boolean isRed;
                    Color suitPoint = new Color(outputImage.getRGB(48, 76));
                    if (isRed(suitPoint)) {
                        System.out.print(new Color(outputImage.getRGB(48, 62)).equals(Color.WHITE) ? "red hearts" : "red diamonds");
                        isRed = true;
                    } else if (darkerThan(suitPoint, 50)) {
                        suitPoint = new Color(outputImage.getRGB(39, 73));
                        System.out.print(darkerThan(suitPoint, 50) ? "black spades" : "black clubs");
                        isRed = false;
                    } else {
                        System.out.print("unknown");
                        isRed = false;
                    }
                    // Find out card value.
                    int cardValue = -1;
                    for (int j = 0; j < describingPoints.size(); j++) {
                        if (describingPoints.get(j).stream().allMatch(point -> PlayingCardsIdentifier.matches(outputImage, point, isRed))) {
                            cardValue = j;
                            break;
                        }
                    }
                    System.out.println(", card value: " + cardValueMappings.get(cardValue));
                    ImageIO.write(outputImage, "png", new File("CSSSRCase\\src\\" + imageFile.getFileName().toString().substring(0, 20) + "\\" + i + ".png"));
                }
                System.out.println(System.currentTimeMillis() - start);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    private static boolean darkerThan(Color point, int value) {
        return point.getRed() < value && point.getGreen() < value && point.getBlue() < value;
    }

    private static boolean isRed(Color point) {
        return point.getRed() > 150 && point.getGreen() < 100 && point.getBlue() < 100;
    }

    private static boolean matches(BufferedImage cardImage, int[] point, boolean isRed) {
        Color colorPoint = new Color(cardImage.getRGB(point[0], point[1]));
        return isRed ? isRed(colorPoint) : darkerThan(colorPoint, 120);
    }
}
