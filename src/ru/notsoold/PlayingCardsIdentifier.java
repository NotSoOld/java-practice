package ru.notsoold;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PlayingCardsIdentifier {

    private static Map<Integer, String> cardValueMappings = new HashMap<Integer, String>() {{
        put(0, "K"); put(1, "A"); put(2, "2"); put(3, "3"); put(4, "4"); put(5, "5"); put(6, "6"); put(7, "7"); put(8, "8"); put(9, "9");
        put(10, "10"); put(11, "J"); put(12, "Q"); put(13, "K"); put(-1, "invalid");
    }};

    private static final List<String> cardsMapping = Arrays.asList("As", "2s", "3s", "4s", "5s", "6s", "7s", "8s", "9s", "10s", "Js", "Qs", "Ks", "Ac", "2c", "3c", "4c", "5c", "6c", "7c", "8c", "9c", "10c", "Jc", "Qc", "Kc", "Ah", "2h", "3h", "4h", "5h", "6h", "7h", "8h", "9h", "10h", "Jh", "Qh", "Kh", "Ad", "2d", "3d", "4d", "5d", "6d", "7d", "8d", "9d", "10d", "Jd", "Qd", "Kd");
    private static final int[][] kernelX = {{ -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 }};
    private static final int[][] kernelY = {{ 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 }};

    private static double[][] weights = new double[25 * 17][52];

    public static void main(String[] args) throws Exception {
        Random r = new Random();
        for (int i = 0; i < 25 * 17; i++) {
            for (int j = 0; j < 52; j++) {
                weights[i][j] = r.nextDouble() / 52;
            }
        }

        List<Path> trainingPlayingCards = Files.walk(Paths.get("CSSSRCase\\src\\cut_cards\\")).collect(Collectors.toList());
        Collections.shuffle(trainingPlayingCards);
        trainingPlayingCards.forEach(imageFile -> {
            try {
                if (Files.isDirectory(imageFile)) {
                    return;
                }
                BufferedImage image = ImageIO.read(imageFile.toFile());
                BufferedImage sobelImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                for (int i = 1; i < image.getWidth() - 1; i++) {
                    for (int j = 1; j < image.getHeight() - 1; j++) {
                        int xKernelConv = convolute(kernelX, image, i, j);
                        int yKernelConv = convolute(kernelY, image, i, j);
                        int result = (int)(Math.sqrt(xKernelConv * xKernelConv + yKernelConv * yKernelConv));
                        sobelImage.setRGB(i, j, (result + (result << 8) + (result << 16)));
                    }
                }

                BufferedImage pooledSobelImage = new BufferedImage(sobelImage.getWidth() / 2, sobelImage.getHeight() / 2, BufferedImage.TYPE_INT_RGB);
                for (int i = 0; i < sobelImage.getWidth() - 1; i+=2) {
                    for (int j = 0; j < sobelImage.getHeight() - 1; j += 2) {
                        pooledSobelImage.setRGB(i/2, j/2, getMaxRgb2x2(sobelImage, i, j));
                    }
                }
                BufferedImage pooledSobelImage2 = new BufferedImage(pooledSobelImage.getWidth() / 2, pooledSobelImage.getHeight() / 2, BufferedImage.TYPE_INT_RGB);
                for (int i = 0; i < pooledSobelImage.getWidth() - 1; i+=2) {
                    for (int j = 0; j < pooledSobelImage.getHeight() - 1; j += 2) {
                        pooledSobelImage2.setRGB(i/2, j/2, getMaxRgb2x2(pooledSobelImage, i, j));
                    }
                }

                int[] imageInput = new int[pooledSobelImage2.getWidth() * pooledSobelImage2.getHeight()];
                pooledSobelImage2.getRGB(0, 0, pooledSobelImage2.getWidth(), pooledSobelImage2.getHeight(),imageInput, 0, 1);
                double[] imageInputNormalized = Arrays.stream(imageInput).mapToDouble(rgb -> (rgb & 0xffffff) / 16777216.0).toArray();
                double[] out = propagateForward(imageInputNormalized);

                double[] softmaxResults = softmax(out);
                int maxProbabilityIndex = IntStream.range(0, softmaxResults.length).reduce((i, j) -> softmaxResults[i] > softmaxResults[j] ? i : j).orElse(-1);
                System.out.print(maxProbabilityIndex != -1 ? ("I guess it's " + cardsMapping.get(maxProbabilityIndex)) : "no maximum");
                System.out.println("; it was " + imageFile.getParent().getFileName().toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });




        /*Random r = new Random();
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
                // Files.createDirectory(new File("CSSSRCase\\src\\" + imageFile.getFileName().toString().substring(0, 20)).toPath());
                for (int i = 0; i < 5; i++) {
                    Image subImage = image.getSubimage((int)(upLeftCornerX * width + nextCardX * width * i), (int)(upLeftCornerY * height), (int)(cardWidth * width), (int)(cardHeight * height)).getScaledInstance(70, 100, Image.SCALE_SMOOTH);
                    final BufferedImage outputImage = new BufferedImage(70, 100, BufferedImage.TYPE_INT_RGB);
                    outputImage.getGraphics().drawImage(subImage, 0, 0, null);
                    // Check if this is the actual card.
                    if (!new Color(outputImage.getRGB(50, 20)).equals(Color.WHITE)) {
                        continue;
                    }
                    // Find out card suit.
                    /*final boolean isRed;
                    Color suitPoint = new Color(outputImage.getRGB(48, 76));
                    if (isRed(suitPoint)) {
                        //System.out.print(new Color(outputImage.getRGB(48, 62)).equals(Color.WHITE) ? "red hearts" : "red diamonds");
                        isRed = true;
                    } else if (darkerThan(suitPoint, 50)) {
                        suitPoint = new Color(outputImage.getRGB(39, 73));
                        //System.out.print(darkerThan(suitPoint, 50) ? "black spades" : "black clubs");
                        isRed = false;
                    } else {
                        //System.out.print("unknown");
                        isRed = false;
                    }
                    // Find out card value.
                    int cardValue = -1;
                    for (int j = 0; j < describingPoints.size(); j++) {
                        if (describingPoints.get(j).stream().allMatch(point -> PlayingCardsIdentifier.matches(outputImage, point, isRed))) {
                            cardValue = j;
                            break;
                        }
                    }*/
                    //System.out.println(", card value: " + cardValueMappings.get(cardValue));
                    // ImageIO.write(outputImage, "png", new File("CSSSRCase\\src\\" + imageFile.getFileName().toString().substring(0, 20) + "\\" + i + ".png"));
                    /*ImageIO.write(outputImage, "png", new File("CSSSRCase\\src\\cut_cards\\" + r.nextInt() + ".png"));
                }
                //System.out.println(System.currentTimeMillis() - start);
            } catch (Exception e) { e.printStackTrace(); }
        });*/
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

    private static int rgbToGrayscale(int rgb) {
        return (((rgb & 0xff0000) >> 16) + ((rgb & 0xff00) >> 8) + (rgb & 0xff)) / 3;
    }

    private static int rgbToHardBW(int rgb) {
        return ( (((rgb & 0xff0000) >> 16) > 120 ? 255 : 0) + (((rgb & 0xff00) >> 8) > 120 ? 255 : 0) + ((rgb & 0xff) > 120 ? 255 : 0)) / 3;
    }

    private static int convolute(int[][] kernel, BufferedImage image, int x, int y) {
        return (kernel[0][0] * rgbToHardBW(image.getRGB(x - 1, y - 1))
            + kernel[0][1] * rgbToHardBW(image.getRGB(x, y - 1))
            + kernel[0][2] * rgbToHardBW(image.getRGB(x + 1, y - 1))
            + kernel[1][0] * rgbToHardBW(image.getRGB(x - 1, y))
            + kernel[1][1] * rgbToHardBW(image.getRGB(x, y))
            + kernel[1][2] * rgbToHardBW(image.getRGB(x + 1, y))
            + kernel[2][0] * rgbToHardBW(image.getRGB(x - 1, y + 1))
            + kernel[2][1] * rgbToHardBW(image.getRGB(x, y + 1))
            + kernel[2][2] * rgbToHardBW(image.getRGB(x + 1, y + 1)));
    }

    private static int getMaxRgb2x2(BufferedImage image, int x, int y) {
        return IntStream.of(image.getRGB(x, y), image.getRGB(x + 1, y), image.getRGB(x, y + 1), image.getRGB(x + 1, y + 1)).max().getAsInt();
    }

    private static double[] softmax(double[] x) {
        double[] expX = Arrays.stream(x).map(Math::exp).toArray();
        double expsSum = Arrays.stream(expX).sum();
        return Arrays.stream(expX).map(expXi -> expXi / expsSum).toArray();
    }

    private static double[] propagateForward(double[] imageInputs) {
        // imageInputs: 25*17
        // weights   : 25*17 x 52
        double[] dotProductResult = new double[52];
        for (int k = 0; k < 52; k++) {
            for (int i = 0; i < 25 * 17; i++) {
                for (int j = 0; j < 25 * 17; j++) {
                    dotProductResult[k] += imageInputs[i] * weights[j][k];
                }
            }
        }
        return dotProductResult;
    }



    /*

    Random r = new Random();
    int[][] randomFilter = new int[3][3];
        for (int a = 0; a < 3; a++) {
        for (int b = 0; b < 3; b++) {
            randomFilter[a][b] = r.nextInt(3) - 1;
        }
    }
    private static final int[][] fillFilter = {{ -2, -1, 2 }, { -1, -1, -2 }, { 0, 2, -2 }};

    for (String suit: new String[] {"s", "c", "h", "d"}) {
        for (String value: new String[] { "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K" }) {
            // Files.createDirectory(new File("CSSSRCase\\src\\cut_cards\\" + value + suit).toPath());
        }
    }

    */

}
