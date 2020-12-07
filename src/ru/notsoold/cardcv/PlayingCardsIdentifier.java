package ru.notsoold.cardcv;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import static ru.notsoold.cardcv.CardCvUtils.*;

public class PlayingCardsIdentifier implements Serializable {

    public static final int CONVOLUTION_LAYER_FILTERS_QUANTITY = 4;
    /** Depends on card size defined in CardsCutter (70 x 100),
     * the number of consecutively used PoolLayers (each of these divides card size by 2)
     * and the number of filters used in ConvolutionLayer ({@link PlayingCardsIdentifier#CONVOLUTION_LAYER_FILTERS_QUANTITY}). */
    private static final int FULLY_CONNECTED_LAYER_WEIGHTS_SIZE = 17 * 25 * CONVOLUTION_LAYER_FILTERS_QUANTITY;

    private ConvolutionLayer convolutionLayer = new ConvolutionLayer(CONVOLUTION_LAYER_FILTERS_QUANTITY);
    private transient PoolLayer poolLayer1, poolLayer2;
    private FullyConnectedLayer fullyConnectedLayer = new FullyConnectedLayer(FULLY_CONNECTED_LAYER_WEIGHTS_SIZE);

    public static void main(String[] args) throws Exception {
        if (Files.exists(Paths.get("PlayingCardsIdentifier.jobj"))) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PlayingCardsIdentifier.jobj"));
            ((PlayingCardsIdentifier)ois.readObject()).main();
        } else {
            new PlayingCardsIdentifier().main();
        }
    }

    public void main() throws Exception {
        List<Path> trainingPlayingCards = Files.walk(Paths.get("CSSSRCase\\src\\cut_cards\\")).filter(Files::isRegularFile).collect(Collectors.toList());
        for (int i = 0; i < 1000; i++) {
            Collections.shuffle(trainingPlayingCards);
            trainingPlayingCards.forEach(imageFile -> {
                try {
                    BufferedImage originalCardImage = ImageIO.read(imageFile.toFile());
                    List<BufferedImage> imageConvolutedByDifferentFilters = convolutionLayer.forward(originalCardImage);
                    poolLayer1 = new PoolLayer(imageConvolutedByDifferentFilters);
                    poolLayer2 = new PoolLayer(poolLayer1.forward());
                    double[] softmaxResults = fullyConnectedLayer.forward(poolLayer2.forward());
                    int maxProbabilityIndex = getIndexOfMaxElement(softmaxResults);
                    String expectedCard = imageFile.getParent().getFileName().toString();
                    if (expectedCard.equals(cardsMapping.get(maxProbabilityIndex))) {
                        System.out.println(expectedCard);
                    }
                    // System.out.print(maxProbabilityIndex != -1 ? ("I guess it's " + cardsMapping.get(maxProbabilityIndex)) : "no maximum");
                    // System.out.println("; it was " + expectedCard + (expectedCard.equals(cardsMapping.get(maxProbabilityIndex)) ? " - correct!" : ""));

                    double[][][] backpropResults = fullyConnectedLayer.backprop(cardsMapping.indexOf(expectedCard), 0.1);
                    backpropResults = poolLayer2.backprop(backpropResults);
                    backpropResults = poolLayer1.backprop(backpropResults);
                    convolutionLayer.backprop(backpropResults, 1000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("PlayingCardsIdentifier.jobj"));
            oos.writeObject(this);
            oos.flush();
            System.out.println("Wrote CNN to the filesystem");
        }
    }
}
